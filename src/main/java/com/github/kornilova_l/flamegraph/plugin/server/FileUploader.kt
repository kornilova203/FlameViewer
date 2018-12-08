package com.github.kornilova_l.flamegraph.plugin.server

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.calltree.fierix.FierixToCallTreeConverter.Companion.isFierixExtension
import com.github.kornilova_l.flamegraph.plugin.server.converters.file.CompressedFlamegraphFileSaver
import com.intellij.util.PathUtil
import java.io.*
import java.nio.file.Paths

private const val partSize = 1_000_000 * 100 // 100MB

class FileUploader {
    private val fileAccumulators = HashMap<String, FileAccumulator>()

    /**
     * IDEA server does not allow to upload big files
     * so file is split and send by parts. Each part is 100MB
     * Client fairly quickly sends all parts of file. Method is synchronized
     * to avoid race condition.
     */
    fun upload(fileName: String, bytes: ByteArray, currentPart: Int, partsCount: Int) {
        synchronized(this) {
            val fileAccumulator = fileAccumulators.computeIfAbsent(fileName) { FileAccumulator(fileName, partsCount) }
            fileAccumulator.add(bytes, currentPart - 1)
            if (fileAccumulator.fullFileReceived()) {
                /* here we are not interested if file will be saved/converted
                 * client will send a request to check if file was saved/converted */
                val file = fileAccumulator.getFile()
                fileAccumulators.remove(fileName)
                val extension = PathUtil.getFileExtension(fileName)
                if (isFierixExtension(extension)) {
                    /* move file to ser files */
                    PluginFileManager.fierixFileSaver.moveToDir(file, fileName)
                    return
                }
                if (tryToConvertFileToAnotherFile(file)) { // this moves file to right place
                    return
                }
                val converterId = FileToCallTracesConverter.isSupported(file) // if this format is supported
                if (converterId != null) { // if supported
                    /* move file to needed directory. */
                    PluginFileManager.moveFileToUploadedFiles(converterId, fileName, file)
                    return // do not delete file
                }
                file.delete()
            }
        }
    }

    private fun tryToConvertFileToAnotherFile(file: File): Boolean {
        for (fileSaver in FileToFileConverterFileSaver.registeredFileSavers) {
            val res = fileSaver.tryToConvert(file)
            if (res) {
                PluginFileManager.moveFileToUploadedFiles(fileSaver.extension, file.name, file)
                return true
            }
        }
        return false
    }

    /**
     * IDEA server does not allow to upload big files
     * so file is split and send by bytes. Each part is 100MB.
     */
    class FileAccumulator(private val fileName: String, partsCount: Int) {
        private val receivedParts = BooleanArray(partsCount)
        private var tempFile = PluginFileManager.tempFileSaver
                .save(ByteArray(0), fileName)!!

        /**
         * @param partIndex index of part [0, partsCount)
         */
        fun add(newBytes: ByteArray, partIndex: Int) {
            val newFile = PluginFileManager.tempFileSaver
                    .save(ByteArray(0), fileName + System.currentTimeMillis())!!
            BufferedInputStream(FileInputStream(tempFile)).use { inputStream ->
                BufferedOutputStream(FileOutputStream(newFile)).use { outputStream ->
                    for (i in 0 until partIndex) { // copy all bytes that are located before new part
                        if (receivedParts[i]) { // if part was received
                            copyOnePart(outputStream, inputStream)
                        }
                    }
                    /* add new part to file */
                    outputStream.write(newBytes)
                    /* copy all other parts */
                    for (i in partIndex + 1 until receivedParts.size) { // write parts that were previously received
                        if (receivedParts[i]) { // if part was received
                            copyOnePart(outputStream, inputStream)
                        }
                    }
                }
            }
            receivedParts[partIndex] = true
            tempFile.delete() // delete previous file
            tempFile = newFile
        }

        private fun copyOnePart(outputStream: OutputStream, inputStream: InputStream) {
            outputStream.write(inputStream.readBytes(partSize))
        }

        fun fullFileReceived(): Boolean = receivedParts.all { it } // all parts received

        fun getFile(): File {
            val parentDir = tempFile.parent
            val fileWithOriginalName = Paths.get(parentDir.toString(), fileName).toFile()
            tempFile.renameTo(fileWithOriginalName)
            return fileWithOriginalName
        }
    }
}

/**
 * Each file-to-file converter must implement it's own FileToFileConverterFileSaver
 * and register is at [FileToFileConverterFileSaver.registeredFileSavers]
 */
abstract class FileToFileConverterFileSaver {
    companion object {
        val registeredFileSavers = listOf(CompressedFlamegraphFileSaver())
    }

    /**
     * Extension must be supported by FileToCallTracesConverter
     */
    abstract val extension: String

    /**
     * Try to convert file
     * If conversion was successful then overwrite content of file
     */
    abstract fun tryToConvert(file: File): Boolean
}
package com.github.kornilova_l.flamegraph.plugin.server

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.tryToConvertFileToFlamegraph
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
            val fileAccumulator = fileAccumulators.computeIfAbsent(fileName, { FileAccumulator(fileName, partsCount) })
            fileAccumulator.add(bytes, currentPart - 1)
            if (fileAccumulator.fullFileReceived()) {
                /* here we are not interested if file will be saved/converted
                 * client will send a request to check if file was saved/converted */
                val file = fileAccumulator.getFile()
                fileAccumulators.remove(fileName)
                if (ProfilerToFlamegraphConverter.getFileExtension(fileName) == "ser") {
                    /* move file to ser files */
                    PluginFileManager.serFileSaver.moveToDir(file, fileName)
                    return
                }
                val res = tryToConvertFileToFlamegraph(file)
                /* if res == true then flamegraph was already saved to another file. And it is save to file.delete() */
                if (!res) { // if no converter was found
                    val converterId = FileToCallTracesConverter.isSupported(file)
                    if (converterId != null) { // if supported
                        /* move file to needed directory. */
                        PluginFileManager.moveFileToUploadedFiles(converterId, fileName, file)
                        return // do not delete file
                    }
                }
                file.delete()
            }
        }
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
            FileInputStream(tempFile).use { inputStream ->
                FileOutputStream(newFile).use { outputStream ->
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

        private fun copyOnePart(outputStream: FileOutputStream, inputStream: FileInputStream) {
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
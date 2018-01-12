package com.github.kornilova_l.flamegraph.plugin.server

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.converters.tryToConvertFileToFlamegraph
import com.github.kornilova_l.flamegraph.plugin.server.trees.FileToCallTracesConverter
import com.intellij.openapi.diagnostic.Logger.getInstance


class FileUploader {
    private val LOG = getInstance(FileUploader::class.java)
    private val fileManager = PluginFileManager.getInstance()
    private val fileAccumulators = HashMap<String, FileAccumulator>()

    /**
     * IDEA server does not allow to upload big files
     * so file is split and send by parts. Each part is 100MB
     * Client fairly quickly sends all parts of file. Method is synchronized
     * to avoid race condition.
     */
    fun upload(fileName: String, bytes: ByteArray, currentPart: Int, partsCount: Int) {
        synchronized(this) {
            val fileAccumulator = fileAccumulators.computeIfAbsent(fileName, { FileAccumulator(partsCount) })
            fileAccumulator.add(bytes, currentPart - 1)
            if (fileAccumulator.allFileReceived()) {
                /* here we are not interested if file will be saved/converted
                 * client will send a request to check if file was saved/converted */
                val allBytes = fileAccumulator.getBytes()
                if (ProfilerToFlamegraphConverter.getFileExtension(fileName) == "ser") {
                    fileManager.serFileSaver.save(allBytes, fileName)
                }
                val tempFile = PluginFileManager.getInstance().tempFileSaver.save(bytes, fileName)
                if (tempFile == null) {
                    LOG.error("Cannot save file to temporal repository: $tempFile")
                    return
                }
                val res = tryToConvertFileToFlamegraph(tempFile)
                if (!res) { // if no converter was found
                    val converterId = FileToCallTracesConverter.isSupported(tempFile)
                    if (converterId != null) { // if supported
                        PluginFileManager.getInstance().saveUploadedFile(converterId, fileName, bytes)
                    }
                }
                tempFile.delete()
                fileAccumulators.remove(fileName)
            }
        }
    }

    /**
     * IDEA server does not allow to upload big files
     * so file is split and send by parts. Each part is 100MB.
     */
    class FileAccumulator(partsCount: Int) {
        private val parts = Array<ByteArray?>(partsCount, { null })

        /**
         * @param partIndex index of part [0, partsCount)
         */
        fun add(bytes: ByteArray, partIndex: Int) {
            parts[partIndex] = bytes
        }

        fun allFileReceived(): Boolean {
            for (filePart in parts) {
                if (filePart == null) {
                    return false
                }
            }
            return true
        }

        fun getBytes(): ByteArray {
            val totalSize = parts.sumBy { it!!.size }
            val bytes = ByteArray(totalSize)
            var currentByte = 0
            for (i in 0 until parts.size) {
                System.arraycopy(parts[i], 0, bytes, currentByte, parts[i]!!.size)
                currentByte += parts[i]!!.size
            }
            return bytes
        }
    }
}
package com.github.kornilova_l.flamegraph.plugin.converters

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

private val fileManager = PluginFileManager.getInstance()

fun convertWithExtensions(fileName: String, bytes: ByteArray): Boolean {
    var isSaved = false
    val file = fileManager.tempFileSaver.save(bytes, fileName)
    if (file != null) {
        val stacks = ProfilerToFlamegraphConverter.convert(file)
        if (stacks != null) {
            isSaved = fileManager.flamegraphFileSaver
                    .save(stacks, fileName) != null
        }
        file.delete()
    }
    return isSaved
}

abstract class ProfilerToFlamegraphConverter {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToFlamegraphConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToFlamegraphConverter")

        fun getFileExtension(fileName: String): String {
            val dot = fileName.indexOf(".")
            if (dot == -1) {
                return ""
            }
            return fileName.substring(dot + 1, fileName.length)
        }

        fun convert(file: File): Map<String, Int>? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.convert(file) ?: return null
        }
    }

    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to flamegraph format
     * File in parameters will be deleted after calling this method
     */
    abstract fun convert(file: File): Map<String, Int>
}

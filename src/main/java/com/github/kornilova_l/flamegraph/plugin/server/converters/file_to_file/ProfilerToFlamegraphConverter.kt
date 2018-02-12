package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file

import com.github.kornilova_l.flamegraph.plugin.server.FileToFileConverterFileSaver
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class FlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = ProfilerToFlamegraphConverter.flamegraphExtension

    override fun tryToConvert(file: File): Boolean {
        val stacks = ProfilerToFlamegraphConverter.convert(file)
        if (stacks != null) {
            BufferedWriter(FileWriter(file)).use { writer ->
                for ((key, value) in stacks) {
                    writer.write(key)
                    writer.write(" ")
                    writer.write(value.toString())
                    writer.write("\n")
                }
            }
            return true
        }
        return false
    }
}

abstract class ProfilerToFlamegraphConverter {
    companion object {
        const val flamegraphExtension = "flamegraph"
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

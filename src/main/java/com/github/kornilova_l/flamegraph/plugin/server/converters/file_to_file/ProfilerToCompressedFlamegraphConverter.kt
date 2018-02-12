package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file

import com.github.kornilova_l.flamegraph.plugin.server.FileToFileConverterFileSaver
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class CompressedFlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = ProfilerToCompressedFlamegraphConverter.cFlamegraphExtension

    override fun tryToConvert(file: File): Boolean {
        val lines = ProfilerToCompressedFlamegraphConverter.convert(file)
        if (lines != null) {
            BufferedWriter(FileWriter(file)).use { writer ->
                for (line in lines) {
                    writer.write(line.name)
                    writer.write(" ")
                    writer.write(line.width.toString())
                    writer.write(" ")
                    writer.write(line.depth.toString())
                    writer.write("\n")
                }
            }
            return true
        }
        return false
    }
}

data class CFlamegraphLine(val name: String, val width: Long, val depth: Int)

abstract class ProfilerToCompressedFlamegraphConverter {
    companion object {
        const val cFlamegraphExtension = "cflamegraph"

        private val EP_NAME = ExtensionPointName.create<ProfilerToCompressedFlamegraphConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToCompressedFlamegraphConverter")

        fun convert(file: File): List<CFlamegraphLine>? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.convert(file) ?: return null
        }
    }

    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to cflamegraph format
     * File in parameters will be deleted after calling this method
     */
    abstract fun convert(file: File): List<CFlamegraphLine>
}

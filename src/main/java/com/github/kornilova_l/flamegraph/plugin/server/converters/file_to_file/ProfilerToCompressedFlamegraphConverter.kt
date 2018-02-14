package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file

import com.github.kornilova_l.flamegraph.plugin.server.FileToFileConverterFileSaver
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class CompressedFlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = ProfilerToCompressedFlamegraphConverter.cFlamegraphExtension

    override fun tryToConvert(file: File): Boolean {
        val cFlamegraph = ProfilerToCompressedFlamegraphConverter.convert(file)
        if (cFlamegraph != null) {
            BufferedWriter(FileWriter(file)).use { writer ->
                writeMap(cFlamegraph.classNames, writer, 'C')
                writeMap(cFlamegraph.methodNames, writer, 'M')
                writeMap(cFlamegraph.descriptions, writer, 'D')
                for (line in cFlamegraph.lines) {
                    if (line.classNameId != null) {
                        writer.write("C=")
                        writer.write(line.classNameId.toString())
                        writer.write(" ")
                    }
                    writer.write("M=")
                    writer.write(line.methodNameId.toString())
                    writer.write(" ")
                    if (line.descId != null) {
                        writer.write("D=")
                        writer.write(line.descId.toString())
                        writer.write(" ")
                    }
                    writer.write("d=")
                    writer.write(line.depth.toString())
                    writer.write(" w=")
                    writer.write(line.width.toString())
                    writer.write("\n")
                }
            }
            return true
        }
        return false
    }

    private fun writeMap(map: Map<String, Int>, writer: BufferedWriter, letter: Char) {
        if (map.isNotEmpty()) {
            writer.write("--$letter-- ${map.size}\n")
            for (entry in map.entries) {
                writer.write(entry.key)
                writer.write(" ")
                writer.write(entry.value.toString())
                writer.write("\n")
            }
        }
    }
}

data class CFlamegraph(val lines: List<CFlamegraphLine>,
                       val classNames: Map<String, Int>,
                       val methodNames: Map<String, Int>,
                       val descriptions: Map<String, Int>)


data class CFlamegraphLine(val classNameId: Int?, val methodNameId: Int, val descId: Int?, val width: Long, val depth: Int)

abstract class ProfilerToCompressedFlamegraphConverter {
    companion object {
        const val cFlamegraphExtension = "cflamegraph"

        private val EP_NAME = ExtensionPointName.create<ProfilerToCompressedFlamegraphConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToCompressedFlamegraphConverter")

        fun convert(file: File): CFlamegraph? {
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
    abstract fun convert(file: File): CFlamegraph
}

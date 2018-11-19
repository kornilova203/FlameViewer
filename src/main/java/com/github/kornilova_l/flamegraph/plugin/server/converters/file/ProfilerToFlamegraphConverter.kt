package com.github.kornilova_l.flamegraph.plugin.server.converters.file

import com.github.kornilova_l.flamegraph.plugin.server.FileToFileConverterFileSaver
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

@Deprecated("The class will be removed when ProfilerToFlamegraphConverter is no longer used")
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

@Deprecated("Implement ProfilerToCompressedFlamegraphConverter instead")
interface ProfilerToFlamegraphConverter {
    companion object {
        const val flamegraphExtension = "flamegraph"
        private val EP_NAME = ExtensionPointName.create<ProfilerToFlamegraphConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToFlamegraphConverter")

        fun convert(file: File): Map<String, Int>? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.convert(file) ?: return null
        }
    }

    fun isSupported(file: File): Boolean

    /**
     * Convert file to flamegraph format
     * File in parameters will be deleted after calling this method
     */
    fun convert(file: File): Map<String, Int>
}

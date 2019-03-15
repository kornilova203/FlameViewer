package com.github.korniloval.flameviewer.converters.flamegraph

import com.github.korniloval.flameviewer.FileToFileConverterFileSaver
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

@Deprecated("The class will be removed when ProfilerToFlamegraphConverterFactory is no longer used")
class FlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = ProfilerToFlamegraphConverterFactory.flamegraphExtension

    override fun tryToConvert(file: File): Boolean {
        val stacks = ProfilerToFlamegraphConverterFactory.convert(file)
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

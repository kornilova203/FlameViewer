package com.github.korniloval.flameviewer.converters.cflamegraph

import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

@Suppress("ArrayInDataClass") // Instances of the class will not be compared
data class CFlamegraph(val lines: List<CFlamegraphLine>,
                       val classNames: Array<String>, // a "map" from id to class name
                       val methodNames: Array<String>, // a "map" from id to method name
                       val descriptions: Array<String>) // a "map" from id to description


data class CFlamegraphLine(val classNameId: Int?, val methodNameId: Int, val descId: Int?, val width: Int, val depth: Int)

interface ProfilerToCFlamegraphConverterFactory {
    companion object {
        const val cFlamegraphExtension = "cflamegraph"

        private val EP_NAME = ExtensionPointName.create<ProfilerToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.profilerToCFlamegraphConverterFactory")

        fun convert(file: File): CFlamegraph? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.create(file)?.convert() ?: return null
        }
    }

    fun isSupported(file: File): Boolean

    /**
     * Convert file to cflamegraph format
     * File in parameters will be deleted after calling this method
     */
    fun create(file: File): ProfilerToCFlamegraphConverter
}

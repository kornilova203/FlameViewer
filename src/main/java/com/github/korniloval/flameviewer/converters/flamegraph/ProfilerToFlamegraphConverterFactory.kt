package com.github.korniloval.flameviewer.converters.flamegraph

import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

@Deprecated("Implement ProfilerToCFlamegraphConverterFactory instead")
interface ProfilerToFlamegraphConverterFactory {
    companion object {
        const val flamegraphExtension = "flamegraph"
        private val EP_NAME = ExtensionPointName.create<ProfilerToFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.profilerToFlamegraphConverterFactory")

        fun convert(file: File): Map<String, Int>? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.create(file)?.convert() ?: return null
        }
    }

    fun isSupported(file: File): Boolean

    /**
     * Convert file to flamegraph format
     * File in parameters will be deleted after calling this method
     */
    fun create(file: File): ProfilerToFlamegraphConverter
}

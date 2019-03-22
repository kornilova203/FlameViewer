package com.github.korniloval.flameviewer.converters.flamegraph

import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.tryConvert
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

@Deprecated("Implement ProfilerToCFlamegraphConverterFactory instead")
interface ProfilerToFlamegraphConverterFactory : ConverterFactory<Map<String, Int>> {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.profilerToFlamegraphConverterFactory")
        private val LOG = Logger.getInstance(ProfilerToFlamegraphConverterFactory::class.java)

        fun convert(file: File): Map<String, Int>? = tryConvert(EP_NAME.extensions, file) { LOG.error(it) }
    }

    override fun create(file: File): ProfilerToFlamegraphConverter?
}

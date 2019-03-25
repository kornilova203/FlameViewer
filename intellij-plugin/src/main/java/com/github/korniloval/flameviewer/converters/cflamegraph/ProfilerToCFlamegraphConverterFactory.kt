package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.tryConvert
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File


interface ProfilerToCFlamegraphConverterFactory : ConverterFactory<CFlamegraph> {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.profilerToCFlamegraphConverterFactory")
        private val LOG = Logger.getInstance(ProfilerToCFlamegraphConverterFactory::class.java)

        fun convert(file: File): CFlamegraph? = tryConvert(EP_NAME.extensions, file) { LOG.error(it) }
    }

    override fun create(file: File): ToCFlamegraphConverter?
}

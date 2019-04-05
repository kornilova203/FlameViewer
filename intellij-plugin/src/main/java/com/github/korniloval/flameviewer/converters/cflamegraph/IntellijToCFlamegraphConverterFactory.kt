package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File


object IntellijToCFlamegraphConverterFactory : ConverterFactory<CFlamegraph> {
    private val EP_NAME = ExtensionPointName.create<ToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.toCFlamegraphConverterFactory")

    override fun create(file: File): Converter<out CFlamegraph>? {
        for (factory in EP_NAME.extensions) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }
}

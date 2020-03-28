package com.github.kornilova203.flameviewer.converters.cflamegraph

import com.github.kornilova203.flameviewer.converters.Converter
import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File


object ToCFlamegraphConverterFactoryIntellij : ConverterFactory<CFlamegraph> {
    private val EP_NAME = ExtensionPointName.create<ToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.toCFlamegraphConverterFactory")

    override fun create(file: File): Converter<out CFlamegraph>? {
        for (factory in EP_NAME.extensions) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }
}

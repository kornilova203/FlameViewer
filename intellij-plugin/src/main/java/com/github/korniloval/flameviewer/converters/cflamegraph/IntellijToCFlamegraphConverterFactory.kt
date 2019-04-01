package com.github.korniloval.flameviewer.converters.cflamegraph

import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File


object IntellijToCFlamegraphConverterFactory : ToCFlamegraphConverterFactory {
    private val EP_NAME = ExtensionPointName.create<ToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.toCFlamegraphConverterFactory")

    override fun create(file: File): ToCFlamegraphConverter? {
        for (factory in EP_NAME.extensions) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }
}

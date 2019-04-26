package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.LoggerAdapter
import com.github.korniloval.flameviewer.converters.isFlamegraph
import com.intellij.openapi.diagnostic.Logger
import java.io.File

class FlamegraphToCFlamegraphConverterFactoryIntellij : ToCFlamegraphConverterFactory {
    private val logger = LoggerAdapter(Logger.getInstance(FlamegraphToCFlamegraphConverterFactoryIntellij::class.java))

    override fun isSupported(file: File) = isFlamegraph(file, logger)
    override fun create(file: File) = if (isSupported(file)) FlamegraphToCFlamegraphConverter(file) else null
}

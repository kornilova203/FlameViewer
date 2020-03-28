package com.github.kornilova203.flameviewer.converters.cflamegraph

import com.github.kornilova203.flameviewer.converters.Converter
import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.github.kornilova203.flameviewer.converters.cflamegraph.JfrToStacksConverter.EXTENSION
import com.github.kornilova203.flameviewer.server.ServerUtil.getFileExtension
import java.io.File

object JfrToCFlamegraphConverterFactoryCli : ConverterFactory<CFlamegraph> {
    override fun isSupported(file: File): Boolean = EXTENSION == getFileExtension(file.name)?.toLowerCase()
    override fun create(file: File): Converter<out CFlamegraph>? {
        if (isSupported(file)) {
            return Converter { indicator ->
                val stacks = JfrToStacksConverter(file).convert(indicator)
                StacksToCFlamegraphConverter(stacks).convert(indicator)
            }
        }
        return null
    }
}

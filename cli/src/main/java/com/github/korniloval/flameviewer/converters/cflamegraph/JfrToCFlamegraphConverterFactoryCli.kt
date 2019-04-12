package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.cflamegraph.JfrToStacksConverter.EXTENSION
import com.github.korniloval.flameviewer.server.ServerUtil.getFileExtension
import java.io.File

object JfrToCFlamegraphConverterFactoryCli : ConverterFactory<CFlamegraph> {
    override fun isSupported(file: File): Boolean = EXTENSION == getFileExtension(file.name)?.toLowerCase()
    override fun create(file: File): Converter<out CFlamegraph>? {
        if (isSupported(file)) {
            return Converter {
                val stacks = JfrToStacksConverter(file).convert()
                StacksToCFlamegraphConverter(stacks).convert()
            }
        }
        return null
    }
}

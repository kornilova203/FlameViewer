package com.github.korniloval.flameviewer.converters.trees

import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.ConverterFactory
import java.io.File

interface ToTreesSetConverterFactory : ConverterFactory<TreesSet> {
    @Throws(ConversionException::class)
    override fun create(file: File): ToTreesSetConverter?
}

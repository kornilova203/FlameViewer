package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.calltraces.StacksParser
import java.io.File

class FlamegraphToCFlamegraphConverter(private val file: File) : Converter<CFlamegraph> {
    override fun convert(): CFlamegraph {
        val stacks = StacksParser.getStacks(file) ?: throw ConversionException("Cannot get stacks from file $file")
        return StacksToCFlamegraphConverter(stacks).convert()
    }
}

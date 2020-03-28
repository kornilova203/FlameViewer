package com.github.kornilova203.flameviewer.converters.cflamegraph

import com.github.kornilova203.flameviewer.FlameIndicator
import com.github.kornilova203.flameviewer.converters.ConversionException
import com.github.kornilova203.flameviewer.converters.Converter
import com.github.kornilova203.flameviewer.converters.calltraces.StacksParser
import java.io.File

class FlamegraphToCFlamegraphConverter(private val file: File) : Converter<CFlamegraph> {
    override fun convert(indicator: FlameIndicator?): CFlamegraph {
        val stacks = StacksParser.getStacks(file, indicator) ?: throw ConversionException("Cannot get stacks from file $file")
        return StacksToCFlamegraphConverter(stacks).convert(indicator)
    }
}

package com.github.korniloval.flameviewer.converters.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import java.io.File

class ToTreesSetConverterFactory(private val toCallTree: ConverterFactory<TreesProtos.Trees>,
                                 private val toCallTraces: ConverterFactory<TreeProtos.Tree>) : ConverterFactory<TreesSet> {
    @Throws(ConversionException::class)
    override fun create(file: File): Converter<TreesSet>? {
        val toCallTreeConverter = toCallTree.create(file)
        if (toCallTreeConverter != null) {
            return Converter { indicator -> TreesSetImpl(toCallTreeConverter.convert(indicator)) }
        }
        val toCallTracesConverter = toCallTraces.create(file) ?: return null
        return Converter { indicator -> TreesSetImpl(toCallTracesConverter.convert(indicator)) }
    }
}

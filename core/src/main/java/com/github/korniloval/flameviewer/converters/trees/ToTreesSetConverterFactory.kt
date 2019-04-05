package com.github.korniloval.flameviewer.converters.trees

import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.calltraces.ToCallTracesConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.ToCallTreeConverterFactory
import java.io.File

class ToTreesSetConverterFactory(private val toCallTree: ToCallTreeConverterFactory,
                                 private val toCallTraces: ToCallTracesConverterFactory) : ConverterFactory<TreesSet> {
    @Throws(ConversionException::class)
    override fun create(file: File): Converter<TreesSet>? {
        val toCallTreeConverter = toCallTree.create(file)
        if (toCallTreeConverter != null) {
            return Converter { TreesSetImpl(toCallTreeConverter.convert()) }
        }
        val toCallTracesConverter = toCallTraces.create(file) ?: return null
        return Converter { TreesSetImpl(toCallTracesConverter.convert()) }
    }
}

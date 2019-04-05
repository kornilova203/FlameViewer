package com.github.korniloval.flameviewer.converters.trees

import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.calltraces.IntellijToCallTracesConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.IntellijToCallTreeConverterFactory
import java.io.File

object IntellijToTreesSetConverterFactory : ToTreesSetConverterFactory {
    override fun create(file: File): Converter<TreesSet>? {
        val toCallTreeConverter = IntellijToCallTreeConverterFactory.create(file)
        if (toCallTreeConverter != null) {
            return Converter { TreesSetImpl(toCallTreeConverter.convert()) }
        }
        val toCallTracesConverter = IntellijToCallTracesConverterFactory.create(file) ?: return null
        return Converter { TreesSetImpl(toCallTracesConverter.convert()) }
    }
}

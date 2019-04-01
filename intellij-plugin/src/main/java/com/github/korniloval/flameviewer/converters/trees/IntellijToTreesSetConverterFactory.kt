package com.github.korniloval.flameviewer.converters.trees

import com.github.korniloval.flameviewer.converters.calltraces.IntellijToCallTracesConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.IntellijToCallTreeConverterFactory
import java.io.File

object IntellijToTreesSetConverterFactory : ToTreesSetConverterFactory {
    override fun create(file: File): ToTreesSetConverter? {
        val toCallTreeConverter = IntellijToCallTreeConverterFactory.create(file)
        if (toCallTreeConverter != null) {
            return object : ToTreesSetConverter {
                override fun convert(): TreesSet = TreesSetImpl(toCallTreeConverter.convert())
            }
        }
        val toCallTracesConverter = IntellijToCallTracesConverterFactory.create(file)
        if (toCallTracesConverter != null) {
            return object : ToTreesSetConverter {
                override fun convert(): TreesSet = TreesSetImpl(toCallTracesConverter.convert())
            }
        }
        return null
    }
}
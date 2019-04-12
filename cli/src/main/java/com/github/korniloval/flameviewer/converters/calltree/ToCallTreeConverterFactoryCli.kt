package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import java.io.File

object ToCallTreeConverterFactoryCli : ConverterFactory<TreesProtos.Trees> {
    private val factories = listOf(FierixToCallTreeConverterFactory)

    override fun create(file: File): Converter<out TreesProtos.Trees>? {
        for (factory in factories) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }

}

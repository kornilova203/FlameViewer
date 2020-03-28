package com.github.kornilova203.flameviewer.converters.calltree

import com.github.kornilova203.flameviewer.converters.Converter
import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.github.kornilova_l.flamegraph.proto.TreesProtos
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

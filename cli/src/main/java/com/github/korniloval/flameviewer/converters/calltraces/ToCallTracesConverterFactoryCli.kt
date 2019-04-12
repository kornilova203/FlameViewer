package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.cflamegraph.JfrToCFlamegraphConverterFactoryCli
import com.github.korniloval.flameviewer.converters.cflamegraph.YourkitCsvToCFlamegraphConverterFactory
import java.io.File

object ToCallTracesConverterFactoryCli : ConverterFactory<TreeProtos.Tree> {
    private val factories = listOf(
            CFlamegraphToCallTracesConverterFactory,
            FlamegraphToCallTracesConverterFactory
    )
    private val cflamegraphFactories = listOf(
            JfrToCFlamegraphConverterFactoryCli,
            YourkitCsvToCFlamegraphConverterFactory
    )

    override fun create(file: File): Converter<out TreeProtos.Tree>? {
        for (factory in cflamegraphFactories) {
            val converter = factory.create(file)
            if (converter != null) return Converter { CFlamegraphToCallTracesConverter(converter.convert()).convert() }
        }
        for (factory in factories) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }

}

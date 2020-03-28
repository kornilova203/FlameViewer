package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.Converter
import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.github.kornilova203.flameviewer.converters.cflamegraph.FlamegraphToCFlamegraphConverterFactoryCli
import com.github.kornilova203.flameviewer.converters.cflamegraph.JfrToCFlamegraphConverterFactoryCli
import com.github.kornilova203.flameviewer.converters.cflamegraph.YourkitCsvToCFlamegraphConverterFactory
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import java.io.File

object ToCallTracesConverterFactoryCli : ConverterFactory<TreeProtos.Tree> {
    private val cflamegraphFactories = listOf(
            JfrToCFlamegraphConverterFactoryCli,
            YourkitCsvToCFlamegraphConverterFactory,
            FlamegraphToCFlamegraphConverterFactoryCli
    )

    private val factories = listOf(
            CFlamegraphToCallTracesConverterFactory
    )

    override fun create(file: File): Converter<out TreeProtos.Tree>? {
        for (factory in cflamegraphFactories) {
            val converter = factory.create(file)
            if (converter != null) return Converter { indicator -> CFlamegraphToCallTracesConverter(converter.convert(indicator)).convert(indicator) }
        }
        for (factory in factories) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
        return null
    }

}

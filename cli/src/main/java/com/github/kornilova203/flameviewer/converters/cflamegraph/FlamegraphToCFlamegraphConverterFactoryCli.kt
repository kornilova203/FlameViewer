package com.github.kornilova203.flameviewer.converters.cflamegraph

import com.github.kornilova203.flameviewer.cli.CliLogger
import com.github.kornilova203.flameviewer.converters.isFlamegraph
import java.io.File

object FlamegraphToCFlamegraphConverterFactoryCli : ToCFlamegraphConverterFactory {
    private val logger = CliLogger()

    override fun isSupported(file: File) = isFlamegraph(file, logger)
    override fun create(file: File) = if (isSupported(file)) FlamegraphToCFlamegraphConverter(file) else null
}

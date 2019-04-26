package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.cli.CliLogger
import com.github.korniloval.flameviewer.converters.calltraces.StacksParser.isFlamegraph
import java.io.File

object FlamegraphToCFlamegraphConverterFactoryCli : ToCFlamegraphConverterFactory {
    private val logger = CliLogger()

    override fun isSupported(file: File) = isFlamegraph(file, logger)
    override fun create(file: File) = if (isSupported(file)) FlamegraphToCFlamegraphConverter(file) else null
}

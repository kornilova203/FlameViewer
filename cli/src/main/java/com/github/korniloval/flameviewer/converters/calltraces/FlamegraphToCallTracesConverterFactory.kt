package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.cli.CliLogger
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.calltraces.StacksParser.isFlamegraph
import java.io.File

object FlamegraphToCallTracesConverterFactory : ConverterFactory<TreeProtos.Tree> {
    private val logger = CliLogger()

    override fun isSupported(file: File): Boolean = isFlamegraph(file, logger)
    override fun create(file: File) = if (isSupported(file)) FlamegraphToCallTracesConverter(file) else null
}

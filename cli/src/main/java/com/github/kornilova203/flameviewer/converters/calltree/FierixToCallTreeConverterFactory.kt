package com.github.kornilova203.flameviewer.converters.calltree

import com.github.kornilova203.flameviewer.cli.CliLogger
import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.github.kornilova203.flameviewer.converters.calltree.FierixToCallTreeConverter.Companion.isFierixExtension
import com.github.kornilova203.flameviewer.server.ServerUtil.getFileExtension
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import java.io.File


object FierixToCallTreeConverterFactory : ConverterFactory<TreesProtos.Trees> {
    private val logger = CliLogger()

    override fun isSupported(file: File): Boolean = isFierixExtension(getFileExtension(file.name))
    override fun create(file: File) = if (isSupported(file)) FierixToCallTreeConverter(file, logger) else null
}

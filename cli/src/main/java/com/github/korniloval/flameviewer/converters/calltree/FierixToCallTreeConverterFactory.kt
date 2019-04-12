package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.cli.CliLogger
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.FierixToCallTreeConverter.Companion.isFierixExtension
import com.github.korniloval.flameviewer.server.ServerUtil.getFileExtension
import java.io.File


object FierixToCallTreeConverterFactory : ConverterFactory<TreesProtos.Trees> {
    private val logger = CliLogger()

    override fun isSupported(file: File): Boolean = isFierixExtension(getFileExtension(file.name))
    override fun create(file: File) = if (isSupported(file)) FierixToCallTreeConverter(file, logger) else null
}

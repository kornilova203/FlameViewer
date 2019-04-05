package com.github.korniloval.flameviewer.converters.calltree

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.LoggerAdapter
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter.Companion.EXTENSION
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter.Companion.isFierixExtension
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.PathUtil
import java.io.File

class FierixToCallTreeConverterFactory : ToCallTreeIdentifiedConverterFactory {
    private val logger: FlameLogger = LoggerAdapter(Logger.getInstance(FierixToCallTreeConverterFactory::class.java))
    override val id = EXTENSION

    override fun create(file: File) = FierixToCallTreeConverter(file, logger)

    override fun isSupported(file: File): Boolean = isFierixExtension(PathUtil.getFileExtension(file.name))
}

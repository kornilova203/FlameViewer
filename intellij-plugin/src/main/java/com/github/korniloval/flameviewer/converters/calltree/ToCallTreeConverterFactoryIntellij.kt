package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.FierixToCallTreeConverter.Companion.isFierixExtension
import com.github.korniloval.flameviewer.converters.getConverterId
import com.github.korniloval.flameviewer.converters.tryCreate
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.io.FileUtilRt.getExtension
import java.io.File

object ToCallTreeConverterFactoryIntellij : ConverterFactory<TreesProtos.Trees> {
    private val EP_NAME = ExtensionPointName.create<ToCallTreeIdentifiedConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTreeConverterFactory")
    private val LOG = Logger.getInstance(ToCallTreeConverterFactoryIntellij::class.java)

    override fun create(file: File): Converter<out TreesProtos.Trees>? {
        val fierixConverter = checkFierix(file)
        if (fierixConverter != null) return fierixConverter
        return tryCreate(EP_NAME.extensions, file, LOG)
    }

    @Deprecated("Need to check extension because some fierix files are in directories which names != 'fierix'")
    private fun checkFierix(file: File): Converter<out TreesProtos.Trees>? {
        if (isFierixExtension(getExtension(file.name))) {
            return EP_NAME.extensions
                    .firstOrNull { it.id == FierixToCallTreeConverter.EXTENSION }
                    ?.create(file)
        }
        return null
    }

    fun getConverterId(file: File): String? = getConverterId(EP_NAME.extensions, file)
}

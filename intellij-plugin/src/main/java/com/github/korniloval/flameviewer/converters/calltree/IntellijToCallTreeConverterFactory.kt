package com.github.korniloval.flameviewer.converters.calltree

import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter.Companion.isFierixExtension
import com.github.korniloval.flameviewer.converters.getConverterId
import com.github.korniloval.flameviewer.converters.tryCreate
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.io.FileUtilRt.getExtension
import java.io.File

object IntellijToCallTreeConverterFactory : ToCallTreeConverterFactory {
    private val EP_NAME = ExtensionPointName.create<ToCallTreeIdentifiedConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTreeConverterFactory")
    private val LOG = Logger.getInstance(IntellijToCallTreeConverterFactory::class.java)

    override fun create(file: File): ToCallTreeConverter? {
        val fierixConverter = checkFierix(file)
        if (fierixConverter != null) return fierixConverter
        return tryCreate(EP_NAME.extensions, file, LOG) as? ToCallTreeConverter
    }

    @Deprecated("Need to check extension because some fierix files are in directories which names != 'fierix'")
    private fun checkFierix(file: File): ToCallTreeConverter? {
        if (isFierixExtension(getExtension(file.name))) {
            return EP_NAME.extensions
                    .firstOrNull { it.id == FierixToCallTreeConverter.EXTENSION }
                    ?.create(file)
        }
        return null
    }

    fun getConverterId(file: File): String? = getConverterId(EP_NAME.extensions, file)
}

package com.github.korniloval.flameviewer.converters.calltree

import com.github.korniloval.flameviewer.LoggerAdapter
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverter
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.PathUtil
import java.io.File

class FierixToCallTreeConverterFactory : ToCallTreeIdentifiedConverterFactory {
    override val id = EXTENSION

    override fun create(file: File) = FierixToCallTreeConverter(file, logger)

    override fun isSupported(file: File): Boolean = isFierixExtension(PathUtil.getFileExtension(file.name))

    companion object {
        const val EXTENSION = "fierix"
        private val logger = LoggerAdapter(Logger.getInstance(FierixToCallTreeConverterFactory::class.java))

        fun isFierixExtension(fileExtension: String?): Boolean {
            /* .ser is an old deprecated extension */
            return StringUtil.equalsIgnoreCase(fileExtension, EXTENSION) ||
                    StringUtil.equalsIgnoreCase(fileExtension, "ser")
        }
    }
}

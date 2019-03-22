package com.github.korniloval.flameviewer.converters.calltree.fierix

import com.github.korniloval.flameviewer.converters.calltree.FileToCallTreeConverterFactory
import com.intellij.openapi.util.text.StringUtil
import java.io.File

class FierixToCallTreeConverterFactory : FileToCallTreeConverterFactory {

    override val id = EXTENSION

    override fun create(file: File) = FierixToCallTreeConverter(file)

    companion object {
        const val EXTENSION = "fierix"

        fun isFierixExtension(fileExtension: String?): Boolean {
            /* .ser is an old deprecated extension */
            return StringUtil.equalsIgnoreCase(fileExtension, EXTENSION) ||
                    StringUtil.equalsIgnoreCase(fileExtension, "ser")
        }
    }
}

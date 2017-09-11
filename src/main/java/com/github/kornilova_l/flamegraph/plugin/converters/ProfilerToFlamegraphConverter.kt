package com.github.kornilova_l.flamegraph.plugin.converters

import com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees.StacksParser
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

abstract class ProfilerToFlamegraphConverter {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToFlamegraphConverter>("com.github.com.kornilovaL.profilerToFlamegraphConverter")

        fun getFileExtension(fileName: String): String {
            val dot = fileName.indexOf(".")
            if (dot == -1) {
                return ""
            }
            return fileName.substring(dot + 1, fileName.length)
        }

        fun convert(file: File): ByteArray? {
            val bytes = EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.convert(file) ?: return null
            return if (StacksParser.isFlamegraph(bytes)) bytes else null
        }
    }

    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to flamegraph format
     * File in parameters will be deleted after calling this method
     */
    abstract fun convert(file: File): ByteArray
}

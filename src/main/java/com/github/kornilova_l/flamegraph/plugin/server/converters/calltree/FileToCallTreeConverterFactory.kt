package com.github.kornilova_l.flamegraph.plugin.server.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call tree
 */
interface FileToCallTreeConverterFactory {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTreeConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTreeConverterFactory")

        fun convert(converterId: String, file: File): TreesProtos.Trees? {
            for (extension in EP_NAME.extensions) {
                if (extension.getId() == converterId) {
                    return extension.create(file).convert()
                }
            }
            return null
        }

        fun isSupported(file: File): String? {
            for (extension in EP_NAME.extensions) {
                if (extension.isSupported(file)) {
                    return extension.getId()
                }
            }
            return null
        }
    }

    /**
     * String id that will be used as a directory name
     * for supported files.
     */
    fun getId(): String

    /**
     * Is file supported by this builder.
     * This method is called ones when file is uploaded.
     */
    fun isSupported(file: File): Boolean

    /**
     * Convert file to call tree.
     */
    fun create(file: File): FileToCallTreeConverter
}
package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_tree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call tree
 */
abstract class FileToCallTreeConverter {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTreeConverter>("com.github.kornilovaL.flamegraphProfiler.fileToCallTreeConverter")

        fun convert(converterId: String, file: File): TreesProtos.Trees? {
            for (extension in EP_NAME.extensions) {
                if (extension.getId() == converterId) {
                    return extension.convert(file)
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
    abstract fun getId(): String

    /**
     * Is file supported by this builder.
     * This method is called ones when file is uploaded.
     */
    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to call tree.
     */
    abstract fun convert(file: File): TreesProtos.Trees
}
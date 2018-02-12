package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call traces
 */
abstract class FileToCallTracesConverter {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTracesConverter>("com.github.kornilovaL.flamegraphProfiler.fileToCallTracesConverter")

        fun convert(converterId: String, file: File): TreeProtos.Tree? {
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
     * Converts file to call traces tree.
     */
    abstract fun convert(file: File): TreeProtos.Tree
}
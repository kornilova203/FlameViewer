package com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call traces
 */
interface FileToCallTracesConverterFactory {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTracesConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTracesConverterFactory")

        fun convert(converterId: String, file: File): TreeProtos.Tree? {
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
     * Converts file to call traces tree.
     */
    fun create(file: File): FileToCallTracesConverter
}
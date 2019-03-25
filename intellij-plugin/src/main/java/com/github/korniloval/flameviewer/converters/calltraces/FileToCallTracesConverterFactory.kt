package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.IdentifiedConverterFactory
import com.github.korniloval.flameviewer.converters.tryConvert
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call traces
 */
interface FileToCallTracesConverterFactory : IdentifiedConverterFactory<TreeProtos.Tree> {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTracesConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTracesConverterFactory")

        private val LOG = Logger.getInstance(FileToCallTracesConverterFactory::class.java)

        fun convert(converterId: String, file: File): TreeProtos.Tree? {
            return tryConvert(EP_NAME.extensions, converterId, file) { LOG.error(it) }
        }

        fun isSupported(file: File): String? {
            for (extension in EP_NAME.extensions) {
                if (extension.isSupported(file)) {
                    return extension.id
                }
            }
            return null
        }
    }

    /**
     * Is file supported by this builder.
     * This method is called ones when file is uploaded.
     */
    fun isSupported(file: File): Boolean

    override fun create(file: File): ToCallTracesConverter
}
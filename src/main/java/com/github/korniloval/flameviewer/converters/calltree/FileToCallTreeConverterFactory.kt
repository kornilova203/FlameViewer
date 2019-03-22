package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.IdentifiedConverterFactory
import com.github.korniloval.flameviewer.converters.tryConvert
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call tree
 */
interface FileToCallTreeConverterFactory : IdentifiedConverterFactory<TreesProtos.Trees> {

    companion object {
        private val EP_NAME = ExtensionPointName.create<FileToCallTreeConverterFactory>("com.github.kornilovaL.flamegraphProfiler.fileToCallTreeConverterFactory")
        private val LOG = Logger.getInstance(FileToCallTreeConverterFactory::class.java)

        fun convert(converterId: String, file: File): TreesProtos.Trees? {
            return tryConvert(EP_NAME.extensions, converterId, file) { LOG.error(it) }
        }
    }

    override fun create(file: File): FileToCallTreeConverter?
}
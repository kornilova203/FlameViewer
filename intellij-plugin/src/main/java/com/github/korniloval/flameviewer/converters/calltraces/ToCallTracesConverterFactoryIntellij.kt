package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.getConverterId
import com.github.korniloval.flameviewer.converters.tryCreate
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

object ToCallTracesConverterFactoryIntellij : ConverterFactory<TreeProtos.Tree> {
    private val EP_NAME = ExtensionPointName.create<ToCallTracesIdentifiedConverterFactory>("com.github.kornilovaL.flamegraphProfiler.toCallTracesConverterFactory")
    private val LOG = Logger.getInstance(ToCallTracesConverterFactoryIntellij::class.java)

    override fun create(file: File) = tryCreate(EP_NAME.extensions, file, LOG)

    fun getConverterId(file: File): String? = getConverterId(EP_NAME.extensions, file)
}
package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.tryConvert
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

@Suppress("ArrayInDataClass") // Instances of the class will not be compared
data class CFlamegraph(val lines: List<CFlamegraphLine>,
                       val classNames: Array<String>, // a "map" from id to class name
                       val methodNames: Array<String>, // a "map" from id to method name
                       val descriptions: Array<String>) // a "map" from id to description


data class CFlamegraphLine(val classNameId: Int?, val methodNameId: Int, val descId: Int?, val width: Int, val depth: Int)

interface ProfilerToCFlamegraphConverterFactory : ConverterFactory<CFlamegraph> {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToCFlamegraphConverterFactory>("com.github.kornilovaL.flamegraphProfiler.profilerToCFlamegraphConverterFactory")
        private val LOG = Logger.getInstance(ProfilerToCFlamegraphConverterFactory::class.java)

        fun convert(file: File): CFlamegraph? = tryConvert(EP_NAME.extensions, file) { LOG.error(it) }
    }

    override fun create(file: File): ToCFlamegraphConverter?
}

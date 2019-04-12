package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.ConverterFactory
import com.github.korniloval.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.EXTENSION
import com.github.korniloval.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.getCFlamegraphTree
import com.github.korniloval.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.toCFlamegraph
import com.github.korniloval.flameviewer.server.ServerUtil.getFileExtension
import java.io.File

object CFlamegraphToCallTracesConverterFactory : ConverterFactory<TreeProtos.Tree> {
    override fun isSupported(file: File) = EXTENSION == getFileExtension(file.name)?.toLowerCase()

    override fun create(file: File): CFlamegraphToCallTracesConverter? {
        return if (isSupported(file)) CFlamegraphToCallTracesConverter(getCFlamegraphTree(file).toCFlamegraph())
        else null
    }
}

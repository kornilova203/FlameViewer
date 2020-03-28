package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.ConverterFactory
import com.github.kornilova203.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.EXTENSION
import com.github.kornilova203.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.getCFlamegraphTree
import com.github.kornilova203.flameviewer.converters.calltraces.CFlamegraphToCallTracesConverter.Companion.toCFlamegraph
import com.github.kornilova203.flameviewer.server.ServerUtil.getFileExtension
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import java.io.File

object CFlamegraphToCallTracesConverterFactory : ConverterFactory<TreeProtos.Tree> {
    override fun isSupported(file: File) = EXTENSION == getFileExtension(file.name)?.toLowerCase()

    override fun create(file: File): CFlamegraphToCallTracesConverter? {
        return if (isSupported(file)) CFlamegraphToCallTracesConverter(getCFlamegraphTree(file).toCFlamegraph())
        else null
    }
}

package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.ConverterFactory
import java.io.File

interface ToCallTracesConverterFactory : ConverterFactory<TreeProtos.Tree> {
    override fun create(file: File): ToCallTracesConverter?
}
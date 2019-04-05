package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.ConverterFactory
import java.io.File

interface ToCallTreeConverterFactory : ConverterFactory<TreesProtos.Trees> {
    override fun create(file: File): ToCallTreeConverter?
}

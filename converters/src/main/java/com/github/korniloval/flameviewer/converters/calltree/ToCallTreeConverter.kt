package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.Converter

interface ToCallTreeConverter : Converter<TreesProtos.Trees> {
    override fun convert(): TreesProtos.Trees
}

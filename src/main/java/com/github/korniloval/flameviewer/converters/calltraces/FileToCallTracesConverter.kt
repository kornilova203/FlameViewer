package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.Converter

/**
 * @author Liudmila Kornilova
 **/
interface FileToCallTracesConverter : Converter<TreeProtos.Tree> {
    override fun convert(): TreeProtos.Tree
}
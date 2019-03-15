package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos

/**
 * @author Liudmila Kornilova
 **/
interface FileToCallTracesConverter {
    fun convert(): TreeProtos.Tree
}
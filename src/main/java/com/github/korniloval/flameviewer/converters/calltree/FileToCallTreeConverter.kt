package com.github.korniloval.flameviewer.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos

/**
 * @author Liudmila Kornilova
 **/
interface FileToCallTreeConverter {
    fun convert(): TreesProtos.Trees?
}

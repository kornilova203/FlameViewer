package com.github.kornilova_l.flamegraph.plugin.server.converters.calltree

import com.github.kornilova_l.flamegraph.proto.TreesProtos

/**
 * @author Liudmila Kornilova
 **/
interface FileToCallTreeConverter {
    fun convert(): TreesProtos.Trees?
}

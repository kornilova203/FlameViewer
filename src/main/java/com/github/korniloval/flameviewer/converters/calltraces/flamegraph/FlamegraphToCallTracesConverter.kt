package com.github.korniloval.flameviewer.converters.calltraces.flamegraph

import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverterBase
import com.github.korniloval.flameviewer.converters.calltraces.flamegraph.StacksParser.getStacks
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.openapi.diagnostic.Logger
import java.io.File

/**
 * @author Liudmila Kornilova
 **/
class FlamegraphToCallTracesConverter(file: File) : FileToCallTracesConverterBase(file) {
    override fun convert(): TreeProtos.Tree {
        val stacks = getStacks(file)
        if (stacks == null) {
            LOG.error("Cannot get stacks from file")
            return TreeProtos.Tree.newBuilder().build()
        }
        val tree: TreeProtos.Tree?
        tree = StacksToTreeBuilder(stacks).tree
        if (tree == null) {
            LOG.error("Cannot construct tree. File: $file")
            return TreeProtos.Tree.newBuilder().build()
        }
        return tree
    }

    companion object {
        private val LOG = Logger.getInstance(FlamegraphToCallTracesConverter::class.java)
    }
}

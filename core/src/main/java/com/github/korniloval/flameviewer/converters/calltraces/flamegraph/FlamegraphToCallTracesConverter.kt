package com.github.korniloval.flameviewer.converters.calltraces.flamegraph

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.calltraces.ToCallTracesConverter
import com.github.korniloval.flameviewer.converters.calltraces.flamegraph.StacksParser.getStacks
import java.io.File

/**
 * @author Liudmila Kornilova
 **/
class FlamegraphToCallTracesConverter(private val file: File) : ToCallTracesConverter {
    @Throws(ConversionException::class)
    override fun convert(): TreeProtos.Tree {
        val stacks = getStacks(file) ?: throw ConversionException("Cannot get stacks from file")
        val tree: TreeProtos.Tree?
        tree = StacksToTreeBuilder(stacks).tree
        if (tree == null) {
            throw ConversionException("Cannot construct tree. File: $file")
        }
        return tree
    }
}

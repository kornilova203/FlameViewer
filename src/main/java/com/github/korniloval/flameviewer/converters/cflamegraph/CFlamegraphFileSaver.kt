package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.cflamegraph.Names
import com.github.korniloval.flameviewer.cflamegraph.Node
import com.github.korniloval.flameviewer.cflamegraph.Tree
import com.github.korniloval.flameviewer.FileToFileConverterFileSaver
import com.github.korniloval.flameviewer.converters.calltraces.cflamegraph.CFlamegraphToCallTracesConverterFactory
import com.google.flatbuffers.FlatBufferBuilder
import java.io.File
import java.io.FileOutputStream

class CFlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = CFlamegraphToCallTracesConverterFactory.EXTENSION

    override fun tryToConvert(file: File): Boolean {
        val cFlamegraph = ProfilerToCFlamegraphConverterFactory.convert(file) ?: return false
        val builder = FlatBufferBuilder(1024)

        val classNamesOffsets = IntArray(cFlamegraph.classNames.size)
        for (i in 0 until cFlamegraph.classNames.size) {
            classNamesOffsets[i] = builder.createString(cFlamegraph.classNames[i])
        }
        val classNames = Names.createClassNamesVector(builder, classNamesOffsets)

        val methodNamesOffsets = IntArray(cFlamegraph.methodNames.size)
        for (i in 0 until cFlamegraph.methodNames.size) {
            methodNamesOffsets[i] = builder.createString(cFlamegraph.methodNames[i])
        }
        val methodNames = Names.createMethodNamesVector(builder, methodNamesOffsets)

        val descriptionsOffsets = IntArray(cFlamegraph.descriptions.size)
        for (i in 0 until cFlamegraph.descriptions.size) {
            descriptionsOffsets[i] = builder.createString(cFlamegraph.descriptions[i])
        }
        val descriptions = Names.createDescriptionsVector(builder, descriptionsOffsets)

        val names = Names.createNames(builder, classNames, methodNames, descriptions)

        Tree.startNodesVector(builder, cFlamegraph.lines.size)
        for (i in cFlamegraph.lines.size - 1 downTo 0) {
            val line = cFlamegraph.lines[i]
            Node.createNode(builder, line.classNameId ?: -1, line.methodNameId,
                    line.descId ?: -1, line.width, line.depth)
        }
        val nodes = builder.endVector()

        val tree = Tree.createTree(builder, names, nodes)

        builder.finish(tree)
        FileOutputStream(file).write(builder.sizedByteArray())

        return true
    }
}

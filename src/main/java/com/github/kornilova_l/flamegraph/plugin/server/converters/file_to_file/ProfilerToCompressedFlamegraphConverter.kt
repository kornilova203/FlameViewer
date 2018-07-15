package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file

import com.github.kornilova_l.flamegraph.cflamegraph.Names
import com.github.kornilova_l.flamegraph.cflamegraph.Node
import com.github.kornilova_l.flamegraph.cflamegraph.Tree
import com.github.kornilova_l.flamegraph.plugin.server.FileToFileConverterFileSaver
import com.google.flatbuffers.FlatBufferBuilder
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File
import java.io.FileOutputStream


class CompressedFlamegraphFileSaver : FileToFileConverterFileSaver() {
    override val extension = ProfilerToCompressedFlamegraphConverter.cFlamegraphExtension

    override fun tryToConvert(file: File): Boolean {
        val cFlamegraph = ProfilerToCompressedFlamegraphConverter.convert(file) ?: return false
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

@Suppress("ArrayInDataClass") // Instances of the class will not be compared
data class CFlamegraph(val lines: List<CFlamegraphLine>,
                       val classNames: Array<String>, // a "map" from id to class name
                       val methodNames: Array<String>, // a "map" from id to method name
                       val descriptions: Array<String>) // a "map" from id to description


data class CFlamegraphLine(val classNameId: Int?, val methodNameId: Int, val descId: Int?, val width: Int, val depth: Int)

abstract class ProfilerToCompressedFlamegraphConverter {
    companion object {
        const val cFlamegraphExtension = "cflamegraph"

        private val EP_NAME = ExtensionPointName.create<ProfilerToCompressedFlamegraphConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToCompressedFlamegraphConverter")

        fun convert(file: File): CFlamegraph? {
            return EP_NAME.extensions
                    .firstOrNull { it.isSupported(file) }
                    ?.convert(file) ?: return null
        }
    }

    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to cflamegraph format
     * File in parameters will be deleted after calling this method
     */
    abstract fun convert(file: File): CFlamegraph
}

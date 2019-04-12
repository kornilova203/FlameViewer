package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.cflamegraph.Tree
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.cflamegraph.CFlamegraph
import com.github.korniloval.flameviewer.converters.cflamegraph.CFlamegraphLine
import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import java.io.File
import java.nio.ByteBuffer
import java.util.*


class CFlamegraphToCallTracesConverter(private val cf: CFlamegraph) : Converter<TreeProtos.Tree> {
    private var maxDepth = 0

    override fun convert(): TreeProtos.Tree {
        val tree = createEmptyTree()

        val currentStack = ArrayList<TreeProtos.Tree.Node.Builder>()
        currentStack.add(tree.baseNodeBuilder)

        for (line in cf.lines) {
            processLine(line, currentStack)
        }

        tree.depth = maxDepth
        TreesUtil.setNodesOffsetRecursively(tree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(tree)
        TreesUtil.setNodesCount(tree)
        return tree.build()
    }

    private fun processLine(line: CFlamegraphLine, currentStack: ArrayList<TreeProtos.Tree.Node.Builder>) {
        validateLine(line)

        while (line.depth < currentStack.size) { // if some calls are finished
            currentStack.removeAt(currentStack.size - 1)
        }

        if (line.depth != currentStack.size) {
            throw AssertionError("depth of line cannot increase by 2 or more.")
        }

        val newNode = TreesUtil.updateNodeList(
                currentStack[currentStack.size - 1],
                if (line.classNameId != null && line.classNameId >= 0) cf.classNames[line.classNameId] else "",
                cf.methodNames[line.methodNameId],
                if (line.descId != null && line.descId >= 0) cf.descriptions[line.descId] else "",
                line.width.toLong()
        )
        currentStack.add(newNode)
        if (currentStack.size - 1 > maxDepth) {
            maxDepth = currentStack.size - 1
        }
    }

    private fun validateLine(line: CFlamegraphLine) {
        require(line.depth > 0) { "node depth must be bigger than 0." }
        require(line.width > 0) { "node width must be bigger than 0." }
        require(line.methodNameId >= 0) { "node method name id must be set." }
    }

    private fun createEmptyTree(): TreeProtos.Tree.Builder {
        val tree = TreeProtos.Tree.newBuilder()
        tree.setBaseNode(TreeProtos.Tree.Node.newBuilder())
        return tree
    }

    companion object {
        const val EXTENSION = "cflamegraph"

        fun getCFlamegraphTree(file: File): Tree = Tree.getRootAsTree(ByteBuffer.wrap(file.readBytes()))

        fun Tree.toCFlamegraph(): CFlamegraph {
            val names = names()
            val classNames = convertNamesToArray(names.classNamesLength()) { names.classNames(it) }
            val methodNames = convertNamesToArray(names.methodNamesLength()) { names.methodNames(it) }
            val descriptions = convertNamesToArray(names.descriptionsLength()) { names.descriptions(it) }
            return CFlamegraph(getCFlamegraphLines(), classNames, methodNames, descriptions)
        }

        private fun convertNamesToArray(itemsCount: Int, getter: (Int) -> String): Array<String> {
            val list = mutableListOf<String>()
            for (i in 0 until itemsCount) {
                list.add(getter(i))
            }
            return list.toTypedArray()
        }

        private fun Tree.getCFlamegraphLines(): List<CFlamegraphLine> {
            val list = mutableListOf<CFlamegraphLine>()
            for (i in 0 until nodesLength()) {
                val node = nodes(i)
                list.add(CFlamegraphLine(node.classNameId(), node.methodNameId(), node.descriptionId(), node.width(), node.depth()))
            }
            return list
        }
    }
}
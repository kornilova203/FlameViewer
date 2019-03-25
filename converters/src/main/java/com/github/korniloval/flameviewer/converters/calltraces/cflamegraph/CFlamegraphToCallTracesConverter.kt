package com.github.korniloval.flameviewer.converters.calltraces.cflamegraph

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.cflamegraph.Node
import com.github.korniloval.flameviewer.cflamegraph.Tree
import com.github.korniloval.flameviewer.converters.calltraces.ToCallTracesConverter
import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import java.io.File
import java.nio.ByteBuffer
import java.util.*


class CFlamegraphToCallTracesConverter(file: File) : ToCallTracesConverter {
    private val cflamegraphTree: Tree = Tree.getRootAsTree(ByteBuffer.wrap(file.readBytes()))
    private val classNames: Map<Int, String>
    private val methodNames: Map<Int, String>
    private val descriptions: Map<Int, String>
    private var maxDepth = 0

    init {
        val names = cflamegraphTree.names()
        classNames = convertNamesToMap(names.classNamesLength()) { names.classNames(it) }
        methodNames = convertNamesToMap(names.methodNamesLength()) { names.methodNames(it) }
        descriptions = convertNamesToMap(names.descriptionsLength()) { names.descriptions(it) }
    }

    override fun convert(): TreeProtos.Tree {
        val tree = createEmptyTree()

        val currentStack = ArrayList<TreeProtos.Tree.Node.Builder>()
        currentStack.add(tree.baseNodeBuilder)

        for (i in 0 until cflamegraphTree.nodesLength()) {
            processNode(cflamegraphTree.nodes(i), currentStack)
        }

        tree.depth = maxDepth
        TreesUtil.setNodesOffsetRecursively(tree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(tree)
        TreesUtil.setNodesCount(tree)
        return tree.build()
    }

    private fun convertNamesToMap(itemsCount: Int, getter: (Int) -> String): Map<Int, String> {
        val map = HashMap<Int, String>()
        for (i in 0 until itemsCount) {
            map[i] = getter(i)
        }
        return map
    }

    private fun processNode(node: Node,
                            currentStack: ArrayList<TreeProtos.Tree.Node.Builder>) {
        validateNode(node)

        while (node.depth() < currentStack.size) { // if some calls are finished
            currentStack.removeAt(currentStack.size - 1)
        }

        if (node.depth() != currentStack.size) {
            throw AssertionError("depth of node cannot increase by 2 or more.")
        }

        val newNode = TreesUtil.updateNodeList(
                currentStack[currentStack.size - 1],
                if (node.classNameId() >= 0) classNames[node.classNameId()]!! else "",
                methodNames[node.methodNameId()]!!,
                if (node.descriptionId() >= 0) descriptions[node.descriptionId()]!! else "",
                node.width().toLong()
        )
        currentStack.add(newNode)
        if (currentStack.size - 1 > maxDepth) {
            maxDepth = currentStack.size - 1
        }
    }

    private fun validateNode(node: Node) {
        require(node.depth() > 0) { "node depth must be bigger than 0." }
        require(node.width() > 0) { "node width must be bigger than 0." }
        require(node.methodNameId() >= 0) { "node method name id must be set." }
    }

    private fun createEmptyTree(): TreeProtos.Tree.Builder {
        val tree = TreeProtos.Tree.newBuilder()
        tree.setBaseNode(TreeProtos.Tree.Node.newBuilder())
        return tree
    }
}
package com.github.kornilova203.flameviewer.converters.trees.backtraces

import com.github.kornilova203.flameviewer.converters.trees.*
import com.github.kornilova203.flameviewer.server.handlers.treeBuilder
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import java.util.*

/**
 * This class is used only if calltraces tree contains less than [maximumNodesCount]
 */
class BackTracesBuilder(callTraces: TreeProtos.Tree) : TreeBuilder {
    private val backTraces: TreeProtos.Tree

    init {
        val treeBuilder = treeBuilder(Node.newBuilder())
        buildTreeRecursively(callTraces.baseNode, treeBuilder.baseNodeBuilder, ArrayList(callTraces.depth))
        setNodesOffsetRecursively(treeBuilder.baseNodeBuilder, 0)
        setNodesIndices(treeBuilder.baseNodeBuilder)
        setTreeWidth(treeBuilder)
        setNodesCount(treeBuilder)
        treeBuilder.treeInfoBuilder.timePercent = 1f
        treeBuilder.depth = callTraces.depth
        backTraces = treeBuilder.build()
    }

    private fun buildTreeRecursively(node: Node,
                                     baseNode: Node.Builder,
                                     currentStack: MutableList<Node>) {
        currentStack.add(node)
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            buildTreeRecursively(child, baseNode, currentStack)
        }
        val selfTime = getSelfTime(node)
        if (selfTime > 0) {
            addStack(baseNode, currentStack, selfTime)
        }
        currentStack.removeAt(currentStack.size - 1)
    }

    private fun addStack(baseNode: Node.Builder, currentStack: MutableList<Node>, width: Long) {
        var currentBuilder = baseNode
        for (i in currentStack.size - 1 downTo 1) { // first node is base node
            val nodeInfo = currentStack[i].nodeInfo
            currentBuilder = updateNodeList(currentBuilder, nodeInfo.className,
                    nodeInfo.methodName, nodeInfo.description, width)
        }
    }

    override fun getTree(): TreeProtos.Tree = backTraces
}

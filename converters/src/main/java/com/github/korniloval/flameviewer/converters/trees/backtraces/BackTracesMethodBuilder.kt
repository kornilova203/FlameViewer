package com.github.korniloval.flameviewer.converters.trees.backtraces

import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.updateNodeList
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.converters.trees.TreeBuilder


class BackTracesMethodBuilder(callTraces: Tree, className: String, methodName: String, desc: String) : TreeBuilder {
    private val backTraces: Tree
    private var maxDepth = 0

    init {
        val treeBuilder = createTree(className, methodName, desc)
        val methodNode = treeBuilder.baseNodeBuilder.nodesBuilderList[0]
        buildTreeRecursively(callTraces.baseNode, methodNode, className, methodName, desc, ArrayList(callTraces.depth))
        TreesUtil.setNodesOffsetRecursively(treeBuilder.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(treeBuilder)
        TreesUtil.setNodesCount(treeBuilder)
        treeBuilder.depth = maxDepth
        treeBuilder.treeInfoBuilder.timePercent = treeBuilder.width.toFloat() / callTraces.width
        backTraces = treeBuilder.build()
    }

    /**
     * @return long number - how much time from this stack was added to backtraces
     *         this value is needed when there are multiple occurrences of needed method in one stack,
     *         so only 'free top part' width of each node is added to backtraces.
     */
    private fun buildTreeRecursively(node: Tree.Node,
                                     methodNode: Tree.Node.Builder,
                                     className: String,
                                     methodName: String,
                                     desc: String,
                                     currentStack: MutableList<Tree.Node>): Long {
        var alreadyAddedWidth = 0L
        currentStack.add(node)
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            alreadyAddedWidth += buildTreeRecursively(child, methodNode, className, methodName, desc, currentStack)
        }
        currentStack.removeAt(currentStack.size - 1)
        if (isSameMethod(node, className, methodName, desc)) {
            addStack(methodNode, currentStack, node.width - alreadyAddedWidth)
            return node.width
        }
        return alreadyAddedWidth
    }

    private fun addStack(methodNode: Tree.Node.Builder, currentStack: MutableList<Tree.Node>, width: Long) {
        methodNode.width = methodNode.width + width
        if (currentStack.size > maxDepth) {
            maxDepth = currentStack.size
        }
        var currentBuilder = methodNode
        for (i in currentStack.size - 1 downTo 1) { // first node is base node
            val nodeInfo = currentStack[i].nodeInfo
            currentBuilder = updateNodeList(currentBuilder, nodeInfo.className,
                    nodeInfo.methodName, nodeInfo.description, width)
        }
    }

    /**
     * @return true if class name, method name and description are the same
     */
    private fun isSameMethod(node: Tree.Node,
                             className: String,
                             methodName: String,
                             desc: String): Boolean {
        val nodeBuilderInfo = node.nodeInfo
        return className == nodeBuilderInfo.className &&
                methodName == nodeBuilderInfo.methodName &&
                desc == nodeBuilderInfo.description
    }

    private fun createTree(className: String, methodName: String, desc: String): Tree.Builder {
        val treeBuilder = Tree.newBuilder()
        val methodNode = Tree.Node.newBuilder()
                .setNodeInfo(Tree.Node.NodeInfo.newBuilder()
                        .setClassName(className)
                        .setMethodName(methodName)
                        .setDescription(desc)
                        .build())
        treeBuilder.baseNodeBuilder.addNodes(methodNode)
        return treeBuilder
    }

    override fun getTree(): Tree = backTraces
}
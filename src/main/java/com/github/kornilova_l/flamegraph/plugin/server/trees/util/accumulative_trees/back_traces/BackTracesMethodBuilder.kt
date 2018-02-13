package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.back_traces

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.updateNodeList
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree


class BackTracesMethodBuilder(callTraces: Tree, className: String, methodName: String, desc: String) : TreeBuilder {
    private val backTraces: Tree
    var maxDepth = 0

    init {
        val treeBuilder = createTree(className, methodName, desc)
        val methodNode = treeBuilder.baseNodeBuilder.nodesBuilderList[0]
        for (node in callTraces.baseNode.nodesList) { // avoid base node
            buildTreeRecursively(node, methodNode, className, methodName, desc, 1)
        }
        TreesUtil.setNodesOffsetRecursively(treeBuilder.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(treeBuilder)
        TreesUtil.setNodesCount(treeBuilder)
        treeBuilder.depth = maxDepth
        treeBuilder.treeInfoBuilder.timePercent = treeBuilder.width.toFloat() / callTraces.width
        backTraces = treeBuilder.build()
    }

    /**
     * @return list of node builders.
     *         In each call all children of a node are checked:
     *         1. Do recursive call with a child
     *         2. If list is returned then append current node to children of each item in list. (add created nodes to new list)
     *         3. If child is the searched method
     *              3.1 Expand width of a base node in tree by width of child minus sum of width of all items in returned list
     *                  (this is needed to calculate only 'top' width of nodes)
     *              3.2 Append current node to base node children.
     *              3.3 Add new node to new list
     */
    private fun buildTreeRecursively(node: Tree.Node,
                                     methodNode: Tree.Node.Builder,
                                     className: String,
                                     methodName: String,
                                     desc: String,
                                     depth: Int): MutableList<Tree.Node.Builder>? {
        var list: MutableList<Tree.Node.Builder>? = null
        for (child in node.nodesList) {
            val newList =
                    buildTreeRecursively(child, methodNode, className, methodName, desc, depth + 1)
            if (newList != null) {
                list = list ?: ArrayList()
                for (i in 0 until newList.size) {
                    val previousNode = newList[i]
                    val nodeInfo = node.nodeInfo
                    val updatedNode = updateNodeList(previousNode, nodeInfo.className,
                            nodeInfo.methodName, nodeInfo.description, previousNode.width)
                    list.add(updatedNode) // replace item
                }
            }
            if (isSameMethod(child, className, methodName, desc)) { // if it is the last occurrence in subtree
                if (depth + 1 > maxDepth) {
                    maxDepth = depth + 1
                }
                var width = child.width
                if (newList != null) {
                    for (countedNode in newList) {
                        width -= countedNode.width
                    }
                }
                methodNode.width = methodNode.width + width
                list = list ?: ArrayList()
                val nodeInfo = node.nodeInfo
                list.add(
                        updateNodeList(methodNode, nodeInfo.className, nodeInfo.methodName, nodeInfo.description, width)
                )
            }
        }
        return list
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
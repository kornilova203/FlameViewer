package com.github.korniloval.flameviewer.trees.util

import com.github.korniloval.flameviewer.pleaseReportIssue
import com.github.korniloval.flameviewer.pleaseReportIssue
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node

object TreesUtil {

    fun createNodeInfo(className: String,
                       methodName: String,
                       desc: String): Tree.Node.NodeInfo.Builder {
        return Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param node        node in source tree. Information of this node will be added to building tree
     * @return Node.Builder from building tree which was created or updated
     */
    fun updateNodeList(nodeBuilder: Tree.Node.Builder,
                       node: Tree.Node): Tree.Node.Builder {
        val nodeInfo = node.nodeInfo
        return updateNodeList(nodeBuilder, nodeInfo.className, nodeInfo.methodName,
                nodeInfo.description, node.width)
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param time        time which will be set (or added) to created or updated node
     * (in back traces added time differs for node's time)
     * @return Node.Builder from building tree which was created or updated
     */
    fun updateNodeList(nodeBuilder: Tree.Node.Builder,
                       className: String,
                       methodName: String,
                       description: String,
                       time: Long): Tree.Node.Builder {
        val childCount = nodeBuilder.nodesCount
        val children = nodeBuilder.nodesBuilderList
        val comparableName = className + methodName
        for (i in 0 until childCount) {
            val childNodeBuilder = children[i]
            if (isSameMethod(childNodeBuilder, className, methodName, description)) {
                addTimeToNode(childNodeBuilder, time)
                return childNodeBuilder
            }
            if (comparableName < getComparableName(childNodeBuilder)) { // if insert between
                return addNodeToList(nodeBuilder, className, methodName, description, time, i)
            }
        }
        return addNodeToList(nodeBuilder, className, methodName, description, time, childCount) // no such method and it is biggest
    }

    /**
     * The same as previous function but it uses already existing NodeInfo.
     * So there are no duplicate instances of NodeInfo
     */
    fun updateNodeList(nodeBuilder: Tree.Node.Builder,
                       nodeInfo: Node.NodeInfo,
                       time: Long): Tree.Node.Builder {
        val childCount = nodeBuilder.nodesCount
        val children = nodeBuilder.nodesBuilderList
        val className = nodeInfo.className
        val methodName = nodeInfo.methodName
        val description = nodeInfo.description
        val comparableName = className + methodName
        for (i in 0 until childCount) {
            val childNodeBuilder = children[i]
            if (isSameMethod(childNodeBuilder, className, methodName, description)) {
                addTimeToNode(childNodeBuilder, time)
                return childNodeBuilder
            }
            if (comparableName < getComparableName(childNodeBuilder)) { // if insert between
                return addNodeToList(nodeBuilder, nodeInfo, time, i)
            }
        }
        return addNodeToList(nodeBuilder, nodeInfo, time, childCount) // no such method and it is biggest
    }

    private fun getComparableName(node: Tree.NodeOrBuilder): String {
        val nodeInfo = node.nodeInfo ?: return ""
        return nodeInfo.className + nodeInfo.methodName
    }

    /**
     * Update time in node of full tree
     */
    private fun addTimeToNode(nodeBuilder: Tree.Node.Builder,
                              time: Long) {
        nodeBuilder.width = nodeBuilder.width + time
    }

    private fun addNodeToList(nodeBuilder: Tree.Node.Builder,
                              className: String,
                              methodName: String,
                              desc: String,
                              time: Long,
                              pos: Int): Tree.Node.Builder {
        val newNodeBuilder = createNodeBuilder(className, methodName, desc, time)
        nodeBuilder.addNodes(pos, newNodeBuilder)
        return nodeBuilder.getNodesBuilder(pos)
    }

    private fun addNodeToList(nodeBuilder: Tree.Node.Builder,
                              nodeInfo: Node.NodeInfo,
                              time: Long,
                              pos: Int): Node.Builder {
        val newNodeBuilder = Node.newBuilder().setNodeInfo(nodeInfo).setWidth(time)
        nodeBuilder.addNodes(pos, newNodeBuilder)
        return nodeBuilder.getNodesBuilder(pos)
    }

    private fun createNodeBuilder(className: String,
                                  methodName: String,
                                  desc: String,
                                  time: Long): Tree.Node.Builder {
        return Node.newBuilder()
                .setNodeInfo(
                        createNodeInfo(className, methodName, desc)
                )
                .setWidth(time)
    }

    /**
     * If class name, method name and description are the same return true
     */
    fun isSameMethod(nodeBuilder: Tree.Node.Builder,
                     className: String,
                     methodName: String,
                     desc: String): Boolean {
        val nodeBuilderInfo = nodeBuilder.nodeInfoBuilder
        return methodName == nodeBuilderInfo.methodName &&
                className == nodeBuilderInfo.className &&
                desc == nodeBuilderInfo.description
    }

    /**
     * Set offset of nodes in formed tree
     */
    fun setNodesOffsetRecursively(node: Tree.Node.Builder, offset: Long) {
        var newOffset = offset
        for (childNode in node.nodesBuilderList) {
            childNode.offset = newOffset
            setNodesOffsetRecursively(childNode, newOffset)
            newOffset += childNode.width
        }
    }

    fun setNodesCount(tree: Tree.Builder) {
        val nodesCount = countNodesRecursively(tree.baseNodeBuilder) - 1 // do not count baseNode
        tree.treeInfoBuilder.nodesCount = nodesCount
    }

    private fun countNodesRecursively(node: Node.Builder): Int {
        var nodesCount = 0
        for (child in node.nodesBuilderList) {
            nodesCount += countNodesRecursively(child)
        }
        return nodesCount + 1
    }

    /**
     * This method must be called after offsets of nodes are set
     * [TreesUtil.setNodesOffsetRecursively]
     *
     * @param treeBuilder set width to this tree
     */
    fun setTreeWidth(treeBuilder: Tree.Builder) {
        val baseNode = treeBuilder.baseNodeBuilder
        if (baseNode.nodesCount == 0) { // if tree is empty
            treeBuilder.width = 0
            return
        }
        val lastNode = baseNode.getNodesBuilder(baseNode.nodesCount - 1)
        treeBuilder.width = lastNode.offset + lastNode.width
    }

    /**
     * substring method takes so much time
     * so I decided to implement method that parses integer without need to
     * create substring
     */
    fun parsePositiveInt(line: String, startIndex: Int, endIndex: Int): Int {
        var res = 0
        for (i in startIndex until endIndex) {
            val c = line[i]
            if (c !in '0'..'9') {
                throw NumberFormatException("$pleaseReportIssue Cannot parse number. Line: $line startIndex: $startIndex. endIndex $endIndex")
            }
            res = res * 10 + (c - '0')
        }
        return res
    }

    fun parsePositiveLong(line: String, startIndex: Int, endIndex: Int): Long {
        var res = 0L
        for (i in startIndex until endIndex) {
            val c = line[i]
            if (c !in '0'..'9') {
                throw NumberFormatException("$pleaseReportIssue Cannot parse number. Line: $line startIndex: $startIndex. endIndex $endIndex")
            }
            res = res * 10 + (c - '0')
        }
        return res
    }

    fun getSelfTime(node: Tree.Node): Long {
        var childTime: Long = 0
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            childTime += child.width
        }
        return node.width - childTime
    }
}

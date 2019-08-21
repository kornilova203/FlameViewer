package com.github.korniloval.flameviewer.converters.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.korniloval.flameviewer.server.handlers.countNodes
import com.github.korniloval.flameviewer.server.handlers.treeBuilder
import kotlin.math.max


fun createNodeInfo(className: String,
                   methodName: String,
                   desc: String): Node.NodeInfo.Builder {
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
fun updateNodeList(nodeBuilder: Node.Builder,
                   node: Node): Node.Builder {
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
fun updateNodeList(nodeBuilder: Node.Builder,
                   className: String,
                   methodName: String,
                   description: String,
                   time: Long): Node.Builder {
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
fun updateNodeList(nodeBuilder: Node.Builder,
                   nodeInfo: Node.NodeInfo,
                   time: Long): Node.Builder {
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
private fun addTimeToNode(nodeBuilder: Node.Builder,
                          time: Long) {
    nodeBuilder.width = nodeBuilder.width + time
}

private fun addNodeToList(nodeBuilder: Node.Builder,
                          className: String,
                          methodName: String,
                          desc: String,
                          time: Long,
                          pos: Int): Node.Builder {
    val newNodeBuilder = createNodeBuilder(className, methodName, desc, time)
    nodeBuilder.addNodes(pos, newNodeBuilder)
    return nodeBuilder.getNodesBuilder(pos)
}

private fun addNodeToList(nodeBuilder: Node.Builder,
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
                              time: Long): Node.Builder {
    return Node.newBuilder()
            .setNodeInfo(
                    createNodeInfo(className, methodName, desc)
            )
            .setWidth(time)
}

/**
 * If class name, method name and description are the same return true
 */
fun isSameMethod(nodeBuilder: Node.Builder,
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
fun setNodesOffsetRecursively(node: Node.Builder, offset: Long) {
    var newOffset = offset
    for (childNode in node.nodesBuilderList) {
        childNode.offset = newOffset
        setNodesOffsetRecursively(childNode, newOffset)
        newOffset += childNode.width
    }
}

fun setNodesCount(tree: Tree.Builder) {
    val nodesCount = countNodes(tree.baseNodeBuilder)
    tree.treeInfoBuilder.nodesCount = nodesCount
}

fun setNodesIndices(node: Node.Builder) {
    for ((i, child) in node.nodesBuilderList.withIndex()) {
        child.index = i
        setNodesIndices(child)
    }
}

fun copyNode(node: Node): Node.Builder {
    val nodeBuilder = Node.newBuilder()
    nodeBuilder.width = node.width
    nodeBuilder.nodeInfo = node.nodeInfo
    nodeBuilder.index = node.index
    nodeBuilder.offset = node.offset
    return nodeBuilder
}

/**
 * This method must be called after offsets of nodes are set
 * [setNodesOffsetRecursively]
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
fun parsePositiveInt(line: String, startIndex: Int, endIndex: Int): Int? {
    var res = 0
    for (i in startIndex until endIndex) {
        val c = line[i]
        if (c !in '0'..'9') return null
        res = res * 10 + (c - '0')
    }
    return res
}

fun parsePositiveLong(line: String, startIndex: Int, endIndex: Int): Long? {
    var res = 0L
    for (i in startIndex until endIndex) {
        val c = line[i]
        if (c !in '0'..'9') return null
        res = res * 10 + (c - '0')
    }
    return res
}

fun getSelfTime(node: Node): Long {
    var childTime: Long = 0
    for (i in 0 until node.nodesList.size) {
        val child = node.nodesList[i]
        childTime += child.width
    }
    return node.width - childTime
}

fun countMaxDepth(node: Tree.NodeOrBuilder): Int {
    val maxChildDepth = node.nodesOrBuilderList.fold(0) { max, child -> max(max, countMaxDepth(child)) }
    return maxChildDepth + 1
}

fun filterTree(tree: Tree, filter: Filter): Tree? {
    val filteredTree = treeBuilder().setVisibleDepth(tree.visibleDepth)
    filteredTree.setBaseNode(Node.newBuilder())

    for (child in tree.baseNode.nodesList) {
        buildFilteredTreeRecursively(filteredTree.baseNodeBuilder, child, filter)
    }

    if (filteredTree.baseNodeBuilder.nodesCount == 0) {
        return null
    }

    setNodesOffsetRecursively(filteredTree.baseNodeBuilder, 0)
    setNodesIndices(filteredTree.baseNodeBuilder)
    setTreeWidth(filteredTree)
    setNodesCount(filteredTree)
    filteredTree.treeInfoBuilder.timePercent = calcTime(filteredTree).toFloat() / calcTime(tree) * tree.treeInfo.timePercent
    filteredTree.depth = countMaxDepth(filteredTree.baseNodeBuilder) - 1
    return filteredTree.build()
}

fun filterCallTree(tree: Tree, filter: Filter): Tree? {
    val filteredTree = treeBuilder().setVisibleDepth(tree.visibleDepth)
    filteredTree.setBaseNode(Node.newBuilder())
    filteredTree.treeInfo = tree.treeInfo
    for (child in tree.baseNode.nodesList) {
        buildFilteredCallTreeRecursively(filteredTree.baseNodeBuilder, child, filter)
    }
    if (filteredTree.baseNodeBuilder.nodesCount == 0) {
        return null
    }

    updateOffset(filteredTree)
    setNodesIndices(filteredTree.baseNodeBuilder)
    setTreeWidth(filteredTree)
    setNodesCount(filteredTree)
    filteredTree.treeInfoBuilder.timePercent = calcTime(filteredTree).toFloat() / calcTime(tree) * tree.treeInfo.timePercent
    filteredTree.depth = countMaxDepth(filteredTree.baseNodeBuilder) - 1
    return filteredTree.build()
}

fun calcTime(tree: Tree.Builder): Long {
    var time = 0L
    for (child in tree.baseNodeBuilder.nodesBuilderList) {
        time += child.width
    }
    return time
}

fun calcTime(tree: Tree): Long {
    var time = 0L
    for (child in tree.baseNode.nodesList) {
        time += child.width
    }
    return time
}

/**
 * Subtract offset of first node from all offsets
 *
 * @param filteredTree tree
 */
private fun updateOffset(filteredTree: Tree.Builder) {
    if (filteredTree.baseNodeBuilder.nodesBuilderList.size == 0) {
        return
    }
    val offset = filteredTree.baseNodeBuilder.getNodes(0).offset
    if (offset == 0L) {
        return
    }
    for (node in filteredTree.baseNodeBuilder.nodesBuilderList) {
        updateOffsetRecursively(node, offset)
    }
}

private fun updateOffsetRecursively(node: Node.Builder, offset: Long) {
    node.offset = node.offset - offset
    for (child in node.nodesBuilderList) {
        updateOffsetRecursively(child, offset)
    }
}

/**
 * @param nodeBuilder to this node children will be added
 * @param child       may be added to nodeBuilder if matches filter
 * @param filter      decides if child will be added
 */
private fun buildFilteredTreeRecursively(nodeBuilder: Node.Builder, child: Node, filter: Filter) {
    val newNodeBuilder =
            if (filter.isIncluded(child)) updateNodeList(nodeBuilder, child.nodeInfo, child.width)
            else nodeBuilder

    for (child2 in child.nodesList) {
        buildFilteredTreeRecursively(newNodeBuilder, child2, filter)
    }
}

/**
 * @param nodeBuilder to this node children will be added
 * @param child       may be added to nodeBuilder if matches filter
 * @param filter      decides if child will be added
 */
private fun buildFilteredCallTreeRecursively(nodeBuilder: Node.Builder, child: Node, filter: Filter) {
    val newNodeBuilder = if (filter.isIncluded(child)) {
        nodeBuilder.addNodes(copyNode(child))
        nodeBuilder.nodesBuilderList[nodeBuilder.nodesBuilderList.size - 1]
    } else {
        nodeBuilder
    }

    for (child2 in child.nodesList) {
        buildFilteredCallTreeRecursively(newNodeBuilder, child2, filter)
    }
}

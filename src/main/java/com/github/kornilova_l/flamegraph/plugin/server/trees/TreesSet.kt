package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.MethodAccumulativeTreeBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.back_traces.BackTracesBuilder
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import kotlin.Comparator

abstract class TreesSet {
    private val hotSpots = ArrayList<HotSpot>()
    protected var callTree: TreesProtos.Trees? = null
    protected var callTraces: Tree? = null
    private var backTraces: Tree? = null

    abstract fun getTreesPreview(filter: Filter?): TreesPreview?

    abstract fun getTree(treeType: TreeType,
                         filter: Filter?): Tree?

    fun getTree(treeType: TreeType,
                className: String,
                methodName: String,
                desc: String,
                filter: Filter?): Tree? {
        val tree: Tree?
        when (treeType) {
            TreeManager.TreeType.CALL_TRACES -> {
                getTree(TreeType.CALL_TRACES, null)
                tree = getTreeForMethod(callTraces, className, methodName, desc)
            }
            TreeManager.TreeType.BACK_TRACES -> {
                getTree(TreeType.BACK_TRACES, null)
                tree = getTreeForMethod(backTraces, className, methodName, desc)
            }
        }
        return if (filter == null) {
            tree
        } else {
            filterTree(tree, filter, false)
        }
    }

    abstract fun getCallTree(filter: Filter?): TreesProtos.Trees?

    abstract fun getCallTree(filter: Filter?, threadsIds: List<Int>?): TreesProtos.Trees?

    internal fun getHotSpots(): List<HotSpot> {
        if (hotSpots.size == 0) {
            if (callTraces == null) {
                callTraces = getTree(TreeType.CALL_TRACES, null)
            }
            if (callTraces == null) {
                return LinkedList()
            }
            val hotSpotTreeMap = HashMap<HotSpot, HotSpot>()
            for (node in callTraces!!.baseNode.nodesList) { // avoid baseNode
                getHotSpotsRecursively(node, hotSpotTreeMap)
            }
            hotSpots.addAll(hotSpotTreeMap.values)
            hotSpots.sortWith(Comparator { hotSpot1, hotSpot2 -> java.lang.Float.compare(hotSpot2.relativeTime, hotSpot1.relativeTime) })
        }
        return hotSpots
    }

    private fun getHotSpotsRecursively(node: Node, hotSpotTreeMap: HashMap<HotSpot, HotSpot>) {
        var hotSpot = HotSpot(
                node.nodeInfo.className,
                node.nodeInfo.methodName,
                node.nodeInfo.description
        )
        hotSpot = hotSpotTreeMap.putIfAbsent(hotSpot, hotSpot) ?: hotSpot
        assert(callTraces != null)
        hotSpot.addTime(getSelfTime(node).toFloat() / callTraces!!.width)
        for (child in node.nodesList) {
            getHotSpotsRecursively(child, hotSpotTreeMap)
        }
    }

    protected fun filterTree(tree: Tree?,
                             filter: Filter,
                             isCallTree: Boolean): Tree? {
        val filteredTree = Tree.newBuilder()
        filteredTree.setBaseNode(Node.newBuilder())
        if (isCallTree) {
            filteredTree.treeInfo = tree!!.treeInfo
            buildFilteredCallTreeRecursively(filteredTree.baseNodeBuilder, tree.baseNode, filter)
        } else {
            buildFilteredTreeRecursively(filteredTree.baseNodeBuilder, tree!!.baseNode, filter)
        }
        if (filteredTree.baseNodeBuilder.nodesCount == 0) {
            return null
        }
        val maxDepth = getMaxDepthRecursively(filteredTree.baseNodeBuilder, 0)
        if (!isCallTree) {
            TreesUtil.setNodesOffsetRecursively(filteredTree.baseNodeBuilder, 0)
        } else {
            updateOffset(filteredTree)
        }
        TreesUtil.setTreeWidth(filteredTree)
        TreesUtil.setNodesCount(filteredTree)
        filteredTree.depth = maxDepth
        return filteredTree.build()
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
     * @param node        children of this node will be added to nodeBuilder
     * @param filter      decides if child will be added
     */
    private fun buildFilteredTreeRecursively(nodeBuilder: Node.Builder,
                                             node: Node,
                                             filter: Filter) {

        for (child in node.nodesList) {
            if (filter.isNodeIncluded(child)) {
                val newNode = TreesUtil.updateNodeList(nodeBuilder, child.nodeInfo, child.width)
                buildFilteredTreeRecursively(
                        newNode,
                        child,
                        filter)
            } else {
                buildFilteredTreeRecursively(nodeBuilder, child, filter)
            }
        }
    }

    /**
     * @param nodeBuilder to this node children will be added
     * @param node        children of this node will be added to nodeBuilder
     * @param filter      decides if child will be added
     */
    private fun buildFilteredCallTreeRecursively(nodeBuilder: Node.Builder,
                                                 node: Node,
                                                 filter: Filter) {

        for (child in node.nodesList) {
            if (filter.isNodeIncluded(child)) {
                var newNode = copyNode(child)
                newNode.offset = child.offset
                nodeBuilder.addNodes(newNode)
                newNode = nodeBuilder.nodesBuilderList[nodeBuilder.nodesBuilderList.size - 1]
                buildFilteredCallTreeRecursively(
                        newNode,
                        child,
                        filter)
            } else {
                buildFilteredCallTreeRecursively(nodeBuilder, child, filter)
            }
        }
    }

    private fun copyNode(node: Node): Node.Builder {
        val nodeBuilder = Node.newBuilder()
        nodeBuilder.width = node.width
        nodeBuilder.nodeInfo = node.nodeInfo
        return nodeBuilder
    }

    protected fun getTreeMaybeFilter(treeType: TreeType, filter: Filter?): Tree? {
        when (treeType) {
            TreeManager.TreeType.CALL_TRACES -> {
                return if (filter == null) {
                    callTraces
                } else filterTree(callTraces, filter, false)
            }
            TreeManager.TreeType.BACK_TRACES -> {
                if (callTraces == null) {
                    return null
                }
                if (backTraces == null) {
                    backTraces = BackTracesBuilder(callTraces!!).tree
                }
                return if (filter == null) {
                    backTraces
                } else filterTree(backTraces, filter, false)
            }
            else -> throw IllegalArgumentException("Tree type is not supported")
        }
    }

    class HotSpot internal constructor(private val className: String, private val methodName: String, description: String) : Comparable<HotSpot> {
        private val parameters: Array<String>
        private val retVal: String
        internal var relativeTime = 0f

        init {
            val params = description.substring(1, description.indexOf(")"))
            parameters = params.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            retVal = description.substring(description.indexOf(")") + 1, description.length)
        }

        override fun compareTo(other: TreesSet.HotSpot): Int {
            return (className + methodName + parameters.joinToString("")).compareTo(
                    other.className + other.methodName + other.parameters.joinToString(""))
        }

        internal fun addTime(callRelativeTime: Float) {
            relativeTime += callRelativeTime
        }
    }

    companion object {

        private fun getTreeForMethod(sourceTree: Tree?,
                                     className: String,
                                     methodName: String,
                                     desc: String): Tree? {
            return if (sourceTree == null) {
                null
            } else MethodAccumulativeTreeBuilder(
                    sourceTree, className, methodName, desc
            ).tree
        }

        fun getMaxDepthRecursively(nodeBuilder: Node.Builder, currentDepth: Int): Int {
            var maxDepth = currentDepth
            for (child in nodeBuilder.nodesBuilderList) {
                val newDepth = getMaxDepthRecursively(child, currentDepth + 1)
                if (newDepth > maxDepth) {
                    maxDepth = newDepth
                }
            }
            return maxDepth
        }

        fun getMaxDepthRecursively(nodeBuilder: Node, currentDepth: Int): Int {
            var maxDepth = currentDepth
            for (child in nodeBuilder.nodesList) {
                val newDepth = getMaxDepthRecursively(child, currentDepth + 1)
                if (newDepth > maxDepth) {
                    maxDepth = newDepth
                }
            }
            return maxDepth
        }

        private fun getSelfTime(node: Node): Long {
            var childTime: Long = 0
            for (child in node.nodesList) {
                childTime += child.width
            }
            return node.width - childTime
        }
    }
}

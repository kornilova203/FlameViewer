package com.github.korniloval.flameviewer.trees

import com.github.korniloval.flameviewer.pleaseReportIssue
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.handlers.tree.request.tree.maximumNodesCount
import com.github.korniloval.flameviewer.trees.TreeManager.TreeType
import com.github.korniloval.flameviewer.trees.hot.spots.HotSpot
import com.github.korniloval.flameviewer.trees.hot.spots.HotSpotsBuilder
import com.github.korniloval.flameviewer.trees.util.TreesUtil
import com.github.korniloval.flameviewer.trees.util.accumulative.trees.CallTracesMethodBuilder
import com.github.korniloval.flameviewer.trees.util.accumulative.trees.back.traces.BackTracesBuilder
import com.github.korniloval.flameviewer.trees.util.accumulative.trees.back.traces.BackTracesMethodBuilder
import java.util.*

abstract class TreesSet {
    private var hotSpots: ArrayList<HotSpot>? = null
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
        getTree(TreeType.CALL_TRACES, null) // tree will be filtered later
        val callTraces = callTraces ?: return null
        val tree: Tree? = when (treeType) {
            TreeManager.TreeType.CALL_TRACES -> {
                CallTracesMethodBuilder(callTraces, className, methodName, desc).tree
            }
            TreeManager.TreeType.BACK_TRACES -> {
                BackTracesMethodBuilder(callTraces, className, methodName, desc).tree
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
        var hotSpots = hotSpots
        if (hotSpots == null) {
            val callTraces = getTree(TreeType.CALL_TRACES, null) ?: return ArrayList()
            hotSpots = HotSpotsBuilder(callTraces).hotSpots
        }
        this.hotSpots = hotSpots
        return hotSpots
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

    protected fun getCallTracesMaybeFiltered(filter: Filter?): Tree? {
        return if (filter == null) {
            callTraces
        } else {
            filterTree(callTraces, filter, false)
        }
    }

    protected fun getBackTracesMaybeFiltered(filter: Filter?): Tree? {
        val callTraces = callTraces ?: return null
        if (callTraces.treeInfo.nodesCount > maximumNodesCount) {
            throw IllegalArgumentException("$pleaseReportIssue: Calltraces must contain less than $maximumNodesCount nodes")
        }
        if (backTraces == null) {
            backTraces = BackTracesBuilder(callTraces).tree
        }
        return if (filter == null) {
            backTraces
        } else {
            filterTree(backTraces, filter, false)
        }
    }

    companion object {

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
    }
}

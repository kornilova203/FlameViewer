package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.converters.trees.TreesSet.Companion.getMaxDepthRecursively
import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import com.github.korniloval.flameviewer.converters.trees.maximumNodesCount
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.TreeManager
import com.github.korniloval.flameviewer.server.FindFile
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

abstract class AccumulativeTreeHandler(protected val treeManager: TreeManager, logger: FlameLogger,
                                       private val type: TreeType, findFile: FindFile) : TreeHandler(logger, findFile) {
    override fun getTree(file: File, decoder: QueryStringDecoder): Tree? {
        val filter = getFilter(decoder, logger)

        val methodName = getParameter(decoder, "method")
        val className = getParameter(decoder, "class")
        val desc = getParameter(decoder, "desc")
        val tree = if (methodName != null && className != null && desc != null) {
            treeManager.getTree(file, type, className, methodName, desc, filter)
        } else {
            treeManager.getTree(file, type, filter)
        } ?: return null
        val path = decoder.parameters()["path"] ?: return tree
        return getSubTree(tree, path.map { Integer.parseInt(it) })
    }

    /**
     * At one moment there are only [maximumNodesCount] visible nodes.
     * When node is zoomed, client send request to server and specifies path to the node.
     * @param path to first node of subtree. Each number in path - index of child
     * @return subtree that contains less than [maximumNodesCount]
     * (Note: it would be more convenient to return a subtree that contains more than [maximumNodesCount], so it will be
     * cut in [TreeHandler].
     * But it means that there will be a duplicate of tree, this duplicate might be really large)
     */
    private fun getSubTree(tree: Tree, path: List<Int>): Tree? {
        var currentNode = tree.baseNode
        for (index in path) {
            currentNode = currentNode.getNodes(index)
        }
        val subTree = Tree.newBuilder()
        if (tree.treeInfo.nodesCount > maximumNodesCount) {
            val lastAcceptedLayer = getLastAcceptedLayerIndex(currentNode)
            val base = Tree.Node.newBuilder()
                    .setWidth(currentNode.width)
                    .setOffset(currentNode.offset)
                    .setNodeInfo(currentNode.nodeInfo)
            subTree.baseNodeBuilder.addNodes(base)
            val addedBase = subTree.baseNodeBuilder.getNodesBuilder(subTree.baseNodeBuilder.nodesBuilderList.size - 1)
            cutTree(currentNode, addedBase, 1, lastAcceptedLayer)
            subTree.visibleDepth = lastAcceptedLayer + 1 // + 1 because currentNode was not counted
        } else {
            logger.warn("There is no need to send sub-tree request if tree contains less than $maximumNodesCount nodes")
            subTree.baseNodeBuilder.addNodes(currentNode)
            subTree.visibleDepth = getMaxDepthRecursively(subTree.baseNodeBuilder, 0)
        }

        subTree.depth = getMaxDepthRecursively(currentNode, 1) // first element in path chooses one of trees of base node

        TreesUtil.setNodesOffsetRecursively(subTree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(subTree)
        TreesUtil.setNodesCount(subTree)

        return subTree.build()
    }
}

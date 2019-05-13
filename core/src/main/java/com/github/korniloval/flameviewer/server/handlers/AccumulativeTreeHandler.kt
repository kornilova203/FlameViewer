package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.converters.trees.TreesSet.Companion.getMaxDepthRecursively
import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.copyNode
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.countMaxDepth
import com.github.korniloval.flameviewer.converters.trees.maximumNodesCount
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.TreeManager
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
        val newBaseNode = path.fold(tree.baseNode) { n, i -> n.getNodes(i) }
        val subTree = treeBuilder()
        if (countNodes(newBaseNode) > maximumNodesCount) {
            val maxAllowedWidth = getMaxAllowedWidth(newBaseNode, maximumNodesCount)
            subTree.baseNodeBuilder.addNodes(copyNode(newBaseNode))
            val addedBaseNode = subTree.baseNodeBuilder.getNodesBuilder(subTree.baseNodeBuilder.nodesBuilderList.size - 1)
            decreaseDetailing(newBaseNode, addedBaseNode, maxAllowedWidth)
            subTree.visibleDepth = countMaxDepth(subTree.baseNodeBuilder)
        } else {
            subTree.baseNodeBuilder.addNodes(newBaseNode)
        }

        subTree.depth = getMaxDepthRecursively(newBaseNode, 1) // first element in path chooses one of trees of base node

        TreesUtil.setNodesOffsetRecursively(subTree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(subTree)
        TreesUtil.setNodesCount(subTree)

        return subTree.build()
    }
}

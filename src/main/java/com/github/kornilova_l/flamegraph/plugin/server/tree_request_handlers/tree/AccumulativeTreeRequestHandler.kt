package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.intellij.openapi.diagnostic.Logger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

abstract class AccumulativeTreeRequestHandler internal constructor(urlDecoder: QueryStringDecoder,
                                                                   context: ChannelHandlerContext) : TreeRequestHandler(urlDecoder, context) {
    private val LOG = Logger.getInstance(AccumulativeTreeRequestHandler::class.java)
    abstract val type: TreeType

    override fun getTree(logFile: File): Tree? {
        val methodName = getParameter(urlDecoder, "method")
        val className = getParameter(urlDecoder, "class")
        val desc = getParameter(urlDecoder, "desc")
        val tree = if (methodName != null && className != null && desc != null) {
            TreeManager.getTree(logFile, type, className, methodName,
                    desc, filter)
        } else {
            TreeManager.getTree(logFile, type, filter)
        } ?: return null
        val path = urlDecoder.parameters()["path"] ?: return tree
        return getSubTree(tree, path.map { Integer.parseInt(it) })
    }

    /**
     * At one moment there are only [maximumNodesCount] visible nodes.
     * When node is zoomed, client send request to server and specifies path to the node.
     * @param path to first node of subtree. Each number in path - index of child
     * @return subtree that contains less than [maximumNodesCount]
     * (Note: it would be more convenient to return a subtree that contains more than [maximumNodesCount], so it will be
     * cut in [TreeRequestHandler].
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
            cutTree(currentNode, subTree.baseNodeBuilder, 1, lastAcceptedLayer)
            subTree.visibleDepth = lastAcceptedLayer
        } else {
            LOG.error("There is no need to send sub-tree request if tree contains less than $maximumNodesCount nodes")
            subTree.baseNodeBuilder.addNodes(currentNode)
        }

        subTree.depth = tree.depth - path.size + 1 // first element in path chooses one of trees of base node

        TreesUtil.setNodesOffsetRecursively(subTree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(subTree)
        TreesUtil.setNodesCount(subTree)

        return subTree.build()
    }
}

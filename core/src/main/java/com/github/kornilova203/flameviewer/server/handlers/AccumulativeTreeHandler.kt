package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.converters.trees.*
import com.github.kornilova203.flameviewer.server.ServerOptions
import com.github.kornilova203.flameviewer.server.ServerOptionsProvider
import com.github.kornilova203.flameviewer.server.ServerUtil.getParameter
import com.github.kornilova203.flameviewer.server.TreeManager
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

abstract class AccumulativeTreeHandler(protected val treeManager: TreeManager, logger: FlameLogger,
                                       private val type: TreeType, optionsProvider: ServerOptionsProvider,
                                       findFile: FindFile) : TreeHandler(logger, optionsProvider, findFile) {
    override fun getTree(file: File, decoder: QueryStringDecoder): Tree? {
        val filter = getFilter(decoder, logger)

        val methodName = getParameter(decoder, "method")
        val className = getParameter(decoder, "class")
        val desc = getParameter(decoder, "desc")
        var tree = if (methodName != null) {
            treeManager.getTree(file, type, className ?: "", methodName, desc ?: "")
        } else {
            treeManager.getTree(file, type)
        } ?: return null

        if (filter != null) {
            val includeFullStacktrace = "true".equals(getParameter(decoder, "include-stacktrace"), true)
            tree = filterTree(tree, filter, includeFullStacktrace) ?: return null
        }
        val path = decoder.parameters()["path"] ?: return tree
        return getSubTree(tree, path.map { Integer.parseInt(it) })
    }

    /**
     * At one moment there are only [ServerOptions.maxNumOfVisibleNodes] visible nodes.
     * When node is zoomed, client send request to server and specifies path to the node.
     * @param path to first node of subtree. Each number in path - index of child
     * @return subtree that contains less than [ServerOptions.maxNumOfVisibleNodes]
     * (Note: it would be more convenient to return a subtree that contains more than maxNumOfVisibleNodes, so it will be
     * cut in [TreeHandler].
     * But it means that there will be a duplicate of tree, this duplicate might be really large)
     */
    private fun getSubTree(tree: Tree, path: List<Int>): Tree? {
        val newBaseNode = path.fold(tree.baseNode) { n, i -> n.getNodes(i) }
        val subTree = treeBuilder()
        subTree.baseNodeBuilder.addNodes(newBaseNode)

        subTree.depth = countMaxDepth(newBaseNode)

        setNodesOffsetRecursively(subTree.baseNodeBuilder, 0)
        setTreeWidth(subTree)
        setNodesCount(subTree)
        subTree.treeInfoBuilder.timePercent = tree.treeInfo.timePercent

        return subTree.build()
    }
}

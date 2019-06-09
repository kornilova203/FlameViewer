package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.copyNode
import com.github.korniloval.flameviewer.converters.trees.countMaxDepth
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerOptionsProvider
import com.github.korniloval.flameviewer.server.ServerUtil.sendProto
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


abstract class TreeHandler(protected val logger: FlameLogger, protected val optionsProvider: ServerOptionsProvider, private val findFile: FindFile) : RequestHandlerBase() {

    abstract fun getTree(file: File, decoder: QueryStringDecoder): Tree?

    final override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder))
                ?: throw RequestHandlingException("File not found. Uri: ${decoder.uri()}")
        doProcess(ctx, file, decoder)
        return true
    }

    protected open fun doProcess(ctx: ChannelHandlerContext, file: File, decoder: QueryStringDecoder) {
        var tree = getTree(file, decoder)
        if (tree == null) {
            sendProto(ctx, null, logger)
            return
        }
        val maxNumOfVisibleNodes = optionsProvider.opt().maxNumOfVisibleNodes
        if (tree.treeInfo.nodesCount > maxNumOfVisibleNodes) {
            val path = decoder.parameters()["path"]
            val simplifiedTree = simplifyTree(tree, maxNumOfVisibleNodes, path != null)
            simplifiedTree.visibleDepth = countMaxDepth(simplifiedTree.baseNodeBuilder)
            tree = simplifiedTree.build()
        }
        sendProto(ctx, tree, logger)
    }

    private fun simplifyTree(tree: Tree, maximumNodesCount: Int, isZoomed: Boolean): Tree.Builder {
        val treeBuilder = treeBuilder()
        val baseNode = if (isZoomed) tree.baseNode.nodesList[0] else tree.baseNode
        var baseNodeBuilder = treeBuilder.baseNodeBuilder
        if (isZoomed) {
            baseNodeBuilder.addNodes(copyNode(baseNode))
            baseNodeBuilder = baseNodeBuilder.nodesBuilderList[0]
        }
        val minAllowedWidth = getMinAllowedWidth(baseNode, maximumNodesCount)
        simplifyTree(baseNode, baseNodeBuilder, minAllowedWidth)
        treeBuilder.setTreeInfo(tree.treeInfo)
                .setDepth(tree.depth)
                .setWidth(tree.width)
                .setVisibleDepth(countMaxDepth(treeBuilder.baseNodeBuilder))
        return treeBuilder
    }

    private fun getMinAllowedWidth(node: Tree.Node, maximumNodesCount: Int): Long {
        val widths = mutableListOf<Long>()
        dfs(node) { widths.add(it.width) }
        widths.sortDescending()
        return widths[Math.min(maximumNodesCount - 1, widths.size - 1)]
    }

    private fun simplifyTree(node: Tree.Node, nodeBuilder: Tree.Node.Builder, minAllowedWidth: Long, first: Boolean = true) {
        for (child in node.nodesList) {
            if (!first && child.width < minAllowedWidth) continue // don't skip first layer
            // don't copy subtree of child
            nodeBuilder.addNodes(copyNode(child))
            val addedChild = nodeBuilder.getNodesBuilder(nodeBuilder.nodesBuilderList.size - 1)
            simplifyTree(child, addedChild, minAllowedWidth, false)
        }
    }
}
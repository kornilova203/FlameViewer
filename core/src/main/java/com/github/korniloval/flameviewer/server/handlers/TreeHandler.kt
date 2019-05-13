package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.copyNode
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.countMaxDepth
import com.github.korniloval.flameviewer.converters.trees.maximumNodesCount
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.sendProto
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


abstract class TreeHandler(protected val logger: FlameLogger, private val findFile: FindFile) : RequestHandlerBase() {

    abstract fun getTree(file: File, decoder: QueryStringDecoder): Tree?

    final override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder))
                ?: throw RequestHandlingException("File not found. Uri: ${decoder.uri()}")
        doProcess(ctx, file, decoder)
        return true
    }

    protected open fun doProcess(ctx: ChannelHandlerContext, file: File, decoder: QueryStringDecoder) {
        val tree = getTree(file, decoder)
        if (tree != null && tree.treeInfo.nodesCount > maximumNodesCount) {
            val cutTree = decreaseDetailing(tree, maximumNodesCount)
            sendProto(ctx, cutTree, logger)
        } else {
            sendProto(ctx, tree, logger)
        }
    }

    private fun decreaseDetailing(tree: Tree, maximumNodesCount: Int): Tree {
        val treeBuilder = treeBuilder()
        decreaseDetailing(tree.baseNode, treeBuilder.baseNodeBuilder, getMaxAllowedWidth(tree.baseNode, maximumNodesCount))
        @Suppress("UsePropertyAccessSyntax")
        treeBuilder.setTreeInfo(tree.treeInfo)
                .setDepth(tree.depth)
                .setWidth(tree.width)
                .setVisibleDepth(countMaxDepth(treeBuilder.baseNodeBuilder))
        return treeBuilder.build()
    }

    companion object {

        fun getMaxAllowedWidth(node: Node, maximumNodesCount: Int): Long {
            val widths = mutableListOf<Long>()
            dfs(node) { widths.add(it.width) }
            widths.sortDescending()
            val w = widths[maximumNodesCount - 1]
            for (i in maximumNodesCount - 1 downTo 0) {
                if (widths[i] != w) return widths[i]
            }
            return w
        }

        internal fun decreaseDetailing(node: Node, nodeBuilder: Node.Builder, maxAllowedWidth: Long) {
            for (child in node.nodesList) {
                if (child.width < maxAllowedWidth) continue
                // don't copy subtree of child
                nodeBuilder.addNodes(copyNode(child))
                val addedChild = nodeBuilder.getNodesBuilder(nodeBuilder.nodesBuilderList.size - 1)
                decreaseDetailing(child, addedChild, maxAllowedWidth)
            }
        }

    }
}
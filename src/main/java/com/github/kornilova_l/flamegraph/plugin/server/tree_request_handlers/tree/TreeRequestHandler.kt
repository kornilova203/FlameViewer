package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto
import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.RequestHandler
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File
import java.util.*


abstract class TreeRequestHandler(urlDecoder: QueryStringDecoder,
                                  context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {
    private val maximumNodesCount = 15_000 // amount of visible nodes

    abstract fun getTree(logFile: File): Tree?

    override fun doProcess(logFile: File) {
        val tree = getTree(logFile)
        if (tree != null) {
            if (tree.treeInfo.nodesCount > maximumNodesCount) {
                val cutTree = cutTree(tree)
                sendProto(context, cutTree)
                return
            }
        }
        sendProto(context, tree)
    }

    private fun cutTree(tree: Tree): Tree {
        val lastLayer = getLastAcceptedLayerIndex(tree)
        val treeBuilder = Tree.newBuilder()
        cutTree(tree.baseNode, treeBuilder.baseNodeBuilder, 1, lastLayer)
        @Suppress("UsePropertyAccessSyntax")
        treeBuilder.setTreeInfo(tree.treeInfo)
                .setDepth(tree.depth)
                .setWidth(tree.width)
                .setVisibleDepth(lastLayer)
        return treeBuilder.build()
    }

    /**
     * Returns index of last accepted layer.
     * If returned index is 10 it means that
     * first 10 layers of tree (not including base node layer)
     * contain less than [maximumNodesCount] nodes.
     * If you add 11th layer then there will be more than
     * [maximumNodesCount] nodes.
     */
    private fun getLastAcceptedLayerIndex(tree: Tree): Int {
        var nodesCount = -1 // do not count base node
        var currentLayerIndex = -1 // do not count base node layer
        /* we need to know when layer ends to update currentLayerIndex */
        var currentLayer = LinkedList<Tree.Node>()
        var nextLayer = LinkedList<Tree.Node>()
        currentLayer.add(tree.baseNode)
        while (!currentLayer.isEmpty()) {
            val node = currentLayer.removeFirst()
            nodesCount++
            if (nodesCount > maximumNodesCount) {
                return currentLayerIndex
            }
            for (child in node.nodesList) {
                nextLayer.add(child)
            }
            if (currentLayer.isEmpty()) {
                currentLayer = nextLayer
                nextLayer = LinkedList()
                currentLayerIndex++
            }
        }
        throw IllegalArgumentException("Tree contains less than $maximumNodesCount nodes")
    }

    private fun cutTree(node: Tree.Node, nodeBuilder: Tree.Node.Builder, currentLayer: Int, lastAcceptedLayer: Int) {
        if (currentLayer > lastAcceptedLayer) {
            return
        }
        for (child in node.nodesList) {
            val newChild = Tree.Node.newBuilder()
                    .setWidth(child.width)
                    .setOffset(child.offset)
                    .setNodeInfo(child.nodeInfo)
            nodeBuilder.addNodes(newChild)
            val addedChild = nodeBuilder.getNodesBuilder(nodeBuilder.nodesBuilderList.size - 1)
            cutTree(child, addedChild, currentLayer + 1, lastAcceptedLayer)
        }
    }
}
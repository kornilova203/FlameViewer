package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

abstract class AccumulativeTreeRequestHandler internal constructor(urlDecoder: QueryStringDecoder,
                                                                   context: ChannelHandlerContext) : TreeRequestHandler(urlDecoder, context) {

    abstract val type: TreeType

    override fun getTree(logFile: File): Tree? {
        val methodName = getParameter(urlDecoder, "method")
        val className = getParameter(urlDecoder, "class")
        val desc = getParameter(urlDecoder, "desc")
        return if (methodName != null && className != null && desc != null) {
            TreeManager.getInstance().getTree(logFile, type, className, methodName,
                    desc, filter)
        } else {
            TreeManager.getInstance().getTree(logFile, type, filter)
        }
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto
import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.RequestHandler
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


abstract class TreeRequestHandler(urlDecoder: QueryStringDecoder,
                                  context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {

    abstract fun getTree(logFile: File): Tree?

    override fun doProcess(logFile: File) {
        val tree = getTree(logFile)
        sendProto(context, tree)
    }
}
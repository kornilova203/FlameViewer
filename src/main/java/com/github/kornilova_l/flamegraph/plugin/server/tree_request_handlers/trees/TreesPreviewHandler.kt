package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.trees

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto
import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.RequestHandler
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


class TreesPreviewHandler(urlDecoder: QueryStringDecoder,
                          context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {
    override fun doProcess(logFile: File) {
        val preview = TreeManager.getInstance().getCallTreesPreview(logFile, filter)
        sendProto(context, preview)
    }
}
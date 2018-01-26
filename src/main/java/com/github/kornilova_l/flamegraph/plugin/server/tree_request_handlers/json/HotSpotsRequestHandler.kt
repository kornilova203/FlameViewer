package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.json

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

class HotSpotsRequestHandler(urlDecoder: QueryStringDecoder,
                             context: ChannelHandlerContext) : JsonRequestHandler(urlDecoder, context) {

    override fun getJsonObject(logFile: File): Any? {
        val projectName = getParameter(urlDecoder, "project")
        val fileName = getParameter(urlDecoder, "file")
        if (projectName == null || fileName == null) {
            return null
        }
        return TreeManager.getInstance().getHotSpots(logFile)
    }
}

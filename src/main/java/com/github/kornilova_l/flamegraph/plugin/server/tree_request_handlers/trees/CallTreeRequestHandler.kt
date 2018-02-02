package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.trees

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto
import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.RequestHandler
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File
import java.util.*

class CallTreeRequestHandler(urlDecoder: QueryStringDecoder,
                             context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {
    override fun doProcess(logFile: File) {
        val trees = TreeManager.getCallTree(logFile, filter, getThreadsIds(urlDecoder))
        sendProto(context, trees)
    }

    private fun getThreadsIds(urlDecoder: QueryStringDecoder): List<Int>? {
        if (getParameter(urlDecoder, "threads") == null) {
            return null
        }
        val threadsIds = ArrayList<Int>()
        val parameters = urlDecoder.parameters()["threads"] ?: return ArrayList()
        for (threadId in parameters) {
            threadsIds.add(Integer.parseInt(threadId))
        }
        return threadsIds
    }
}

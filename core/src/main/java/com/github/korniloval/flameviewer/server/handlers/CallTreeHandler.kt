package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger

import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.ServerUtil.sendProto
import com.github.korniloval.flameviewer.server.TreeManager
import com.sun.xml.internal.ws.handler.HandlerException
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.util.*

class CallTreeHandler(private val treeManager: TreeManager, private val logger: FlameLogger, private val findFile: FindFile) : RequestHandler {
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder)) ?: throw HandlerException("File not found. Uri: ${decoder.uri()}")
        val filter = getFilter(decoder, logger)
        val threadsIds = getThreadsIds(decoder)
        val trees = treeManager.getCallTree(file, filter, threadsIds)
        sendProto(ctx, trees, logger)
        return true
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

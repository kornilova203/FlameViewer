package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.RequestHandlingException
import com.github.kornilova203.flameviewer.server.ServerUtil.getParameter
import com.github.kornilova203.flameviewer.server.ServerUtil.sendProto
import com.github.kornilova203.flameviewer.server.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.util.*

class CallTreeHandler(private val treeManager: TreeManager, private val logger: FlameLogger, private val findFile: FindFile) : RequestHandlerBase() {
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder))
                ?: throw RequestHandlingException("File not found. Uri: ${decoder.uri()}")
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

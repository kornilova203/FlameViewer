package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.RequestHandlingException
import com.github.kornilova203.flameviewer.server.ServerUtil.sendJson
import com.github.kornilova203.flameviewer.server.TreeManager
import com.google.gson.Gson
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

class HotSpotsHandler(private val treeManager: TreeManager, private val logger: FlameLogger, private val findFile: FindFile) : RequestHandlerBase() {
    @Throws(RequestHandlingException::class)
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder))
                ?: throw RequestHandlingException("File not found. Uri: ${decoder.uri()}")
        val hotSpots = treeManager.getHotSpots(file)
        sendJson(ctx, Gson().toJson(hotSpots), logger)
        return true
    }
}

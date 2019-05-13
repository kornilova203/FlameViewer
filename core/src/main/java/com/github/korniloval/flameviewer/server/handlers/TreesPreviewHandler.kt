package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.sendProto
import com.github.korniloval.flameviewer.server.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder


class TreesPreviewHandler(private val treeManager: TreeManager, private val logger: FlameLogger, private val findFile: FindFile) : RequestHandlerBase() {
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder))
                ?: throw RequestHandlingException("File not found. Uri: ${decoder.uri()}")
        val filter = getFilter(decoder, logger)
        val preview = treeManager.getCallTreesPreview(file, filter)
        sendProto(ctx, preview, logger)
        return true
    }
}
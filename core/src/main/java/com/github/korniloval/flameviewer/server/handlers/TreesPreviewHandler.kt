package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.FindFile
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendProto
import com.github.korniloval.flameviewer.server.TreeManager
import com.sun.xml.internal.ws.handler.HandlerException
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder


class TreesPreviewHandler(private val treeManager: TreeManager, private val logger: FlameLogger, private val findFile: FindFile) : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val file = findFile(getFileName(decoder)) ?: throw HandlerException("File not found. Uri: ${decoder.uri()}")
        val filter = getFilter(decoder, logger)
        val preview = treeManager.getCallTreesPreview(file, filter)
        sendProto(ctx, preview, logger)
        return true
    }
}
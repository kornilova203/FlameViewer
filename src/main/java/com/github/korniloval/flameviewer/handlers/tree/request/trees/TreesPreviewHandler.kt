package com.github.korniloval.flameviewer.handlers.tree.request.trees

import com.github.korniloval.flameviewer.ProfilerHttpRequestHandler.sendProto
import com.github.korniloval.flameviewer.handlers.tree.request.RequestHandler
import com.github.korniloval.flameviewer.trees.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


class TreesPreviewHandler(urlDecoder: QueryStringDecoder,
                          context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {
    override fun doProcess(logFile: File) {
        val preview = TreeManager.getCallTreesPreview(logFile, filter)
        sendProto(context, preview)
    }
}
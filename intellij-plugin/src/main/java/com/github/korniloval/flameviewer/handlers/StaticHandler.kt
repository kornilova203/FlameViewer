package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.sendBytes
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

class StaticHandler(private val contentType: String) : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileUri = QueryStringDecoder(request.uri()).path()
        val bytes = PluginFileManager.getStaticFile(fileUri)
                ?: throw RequestHandlingException("Cannot find static files. File uri: $fileUri")
        sendBytes(ctx, contentType, bytes)
        return true
    }
}

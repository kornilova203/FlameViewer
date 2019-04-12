package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.server.NAME
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.sendBytes
import com.github.korniloval.flameviewer.server.handlers.CoreUtil.findResource
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

class StaticHandler(private val contentType: String) : RequestHandlerBase() {
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val uri = getFileUri(QueryStringDecoder(request.uri()))
        val bytes = findResource(uri)
                ?: throw RequestHandlingException("Cannot find static files. File uri: $uri")
        sendBytes(ctx, contentType, bytes)
        return true
    }

    companion object {
        private const val REQUEST_PREFIX = "/$NAME/"

        fun getFileUri(decoder: QueryStringDecoder): String {
            return decoder.path().substring(REQUEST_PREFIX.length)
        }
    }
}

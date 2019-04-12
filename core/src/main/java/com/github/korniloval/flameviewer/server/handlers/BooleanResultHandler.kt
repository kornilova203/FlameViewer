package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.ServerUtil.sendJson
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest

class BooleanResultHandler(private val result: Boolean, private val logger: FlameLogger) : RequestHandlerBase() {

    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        sendJson(ctx, "{\"result\": $result}", logger)
        return true
    }
}

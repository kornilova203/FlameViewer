package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.ServerUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest

class DoesFileExistHandler(private val findFile: FindFile, private val logger: FlameLogger) : RequestHandlerBase() {

    /**
     * Sends {"result": true} if file was found
     * and {"result": false} otherwise.
     */
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        val found = fileName != null && findFile(fileName) != null
        ServerUtil.sendJson(ctx, "{\"result\": $found}", logger)
        return true
    }
}

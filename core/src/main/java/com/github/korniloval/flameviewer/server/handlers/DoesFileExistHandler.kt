package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class DoesFileExistHandler(private val findFile: FindFile) : RequestHandler {

    /**
     * Sends code 302 FOUND if file was found
     * and code 404 NOT_FOUND otherwise.
     */
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        if (findFile(fileName) != null) {
            sendStatus(HttpResponseStatus.FOUND, ctx.channel(), "File $fileName was found in uploaded messages")
        } else {
            sendStatus(HttpResponseStatus.NOT_FOUND, ctx.channel(), "File $fileName was NOT found in uploaded messages")
        }
        return true
    }
}

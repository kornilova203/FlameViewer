package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

object DoesFileExistHandler : RequestHandler {

    /**
     * Sends code 302 FOUND if file was found
     * and code 404 NOT_FOUND otherwise.
     */
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        if (PluginFileManager.getLogFile(fileName) != null) {
            sendStatus(HttpResponseStatus.FOUND, ctx.channel(), "File $fileName was found in uploaded messages")
        } else {
            sendStatus(HttpResponseStatus.NOT_FOUND, ctx.channel(), "File $fileName was NOT found in uploaded messages")
        }
        return true
    }
}

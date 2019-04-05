package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class DeleteFileHandler(private val fileManager: PluginFileManager, private val logger: FlameLogger) : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        logger.info("Delete file: $fileName")
        fileManager.deleteFile(fileName)
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

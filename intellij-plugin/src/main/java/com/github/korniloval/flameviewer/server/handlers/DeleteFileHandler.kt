package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class DeleteFileHandler(private val fileManager: PluginFileManager, private val logger: FlameLogger) : RequestHandlerBase() {
    override fun processPost(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        logger.info("Delete file: $fileName")
        fileManager.deleteFile(fileName)
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

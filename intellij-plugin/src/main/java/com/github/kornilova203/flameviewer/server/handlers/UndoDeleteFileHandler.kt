package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.PluginFileManager
import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class UndoDeleteFileHandler(private val fileManager: PluginFileManager, private val logger: FlameLogger) : RequestHandlerBase() {
    override fun processPost(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        logger.info("Undo delete file: $fileName")
        fileManager.undoDeleteFile(fileName)
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

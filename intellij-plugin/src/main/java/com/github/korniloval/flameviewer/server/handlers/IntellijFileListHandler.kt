package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendJson
import com.google.gson.Gson
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest

class IntellijFileListHandler(private val logger: FlameLogger) : RequestHandler {
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        sendJson(ctx, Gson().toJson(PluginFileManager.getFileNameList()), logger)
        return true
    }
}

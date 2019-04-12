package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.cli.CliLogger
import com.github.korniloval.flameviewer.server.FileNameAndDate
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.ServerUtil
import com.google.gson.Gson
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest

class CliFileListHandler : RequestHandlerBase() {
    private val logger = CliLogger()
    private val gson = Gson()

    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        ServerUtil.sendJson(ctx, gson.toJson(emptyList<FileNameAndDate>()), logger)
        return true
    }
}

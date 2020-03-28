package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.server.IntellijTreeManager
import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus


class ConnectionAliveHandler(private val treeManager: IntellijTreeManager) : RequestHandlerBase() {
    override fun processPost(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        treeManager.updateLastTime()
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

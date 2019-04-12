package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.server.IntellijTreeManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus


class ConnectionAliveHandler(private val treeManager: IntellijTreeManager) : RequestHandler {
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        treeManager.updateLastTime()
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

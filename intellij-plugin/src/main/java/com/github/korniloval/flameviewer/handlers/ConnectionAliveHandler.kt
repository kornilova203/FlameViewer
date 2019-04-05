package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import com.github.korniloval.flameviewer.server.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus


class ConnectionAliveHandler(private val treeManager: TreeManager) : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        treeManager.updateLastTime()
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }
}

package com.github.korniloval.flameviewer.server

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

interface RequestHandler {
    /**
     * @return false if there is no RequestHandler for given uri
     */
    @Throws(RequestHandlingException::class)
    fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean

}

package com.github.kornilova203.flameviewer.server

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest

interface RequestHandler {
    /**
     * @return false if there is no RequestHandler for given uri
     */
    @Throws(RequestHandlingException::class)
    fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean

}

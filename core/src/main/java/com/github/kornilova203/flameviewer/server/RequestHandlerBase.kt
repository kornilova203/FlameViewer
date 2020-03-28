package com.github.kornilova203.flameviewer.server

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpRequest

abstract class RequestHandlerBase : RequestHandler {
    /**
     * @return false if there is no RequestHandler for given uri
     */
    @Throws(RequestHandlingException::class)
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        when (request.method()) {
            HttpMethod.GET -> return processGet(request, ctx)
            HttpMethod.POST -> return processPost(request, ctx)
            HttpMethod.DELETE -> return processDelete(request, ctx)
        }
        return false
    }

    protected open fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean = false
    protected open fun processPost(request: HttpRequest, ctx: ChannelHandlerContext): Boolean = false
    protected open fun processDelete(request: HttpRequest, ctx: ChannelHandlerContext): Boolean = false

}

package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.server.RequestHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.util.regex.Pattern

class DelegatingRequestHandler(private val handlers: Map<String, RequestHandler>,
                               private val patternHandlers: List<Pair<Pattern, RequestHandler>>) : RequestHandler {
    override fun process(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val path = decoder.path()
        val handler = handlers[path]
        if (handler != null) return handler.process(request, ctx)
        for (patternHandler in patternHandlers) {
            if (patternHandler.first.matcher(path).matches()) {
                return patternHandler.second.process(request, ctx)
            }
        }
        return false
    }
}

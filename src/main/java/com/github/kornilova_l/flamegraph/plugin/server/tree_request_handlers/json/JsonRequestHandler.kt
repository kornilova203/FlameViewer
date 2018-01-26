package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.json

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler
import com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.RequestHandler
import com.google.gson.Gson
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


abstract class JsonRequestHandler(urlDecoder: QueryStringDecoder,
                                  context: ChannelHandlerContext) : RequestHandler(urlDecoder, context) {

    /**
     * null if json object is empty
     */
    abstract fun getJsonObject(logFile: File): Any?

    override fun doProcess(logFile: File) {
        val jsonObject = getJsonObject(logFile)
        if (jsonObject == null) {
            ProfilerHttpRequestHandler.sendJson(context, "{}") // send empty object
        } else {
            ProfilerHttpRequestHandler.sendJson(context, Gson().toJson(jsonObject))
        }
    }

}
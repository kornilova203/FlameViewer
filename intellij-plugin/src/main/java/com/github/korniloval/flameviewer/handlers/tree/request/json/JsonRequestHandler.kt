package com.github.korniloval.flameviewer.handlers.tree.request.json

import com.github.korniloval.flameviewer.ProfilerHttpRequestHandler
import com.github.korniloval.flameviewer.handlers.tree.request.RequestHandler
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
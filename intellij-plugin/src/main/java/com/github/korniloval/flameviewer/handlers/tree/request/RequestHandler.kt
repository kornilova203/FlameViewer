package com.github.korniloval.flameviewer.handlers.tree.request

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.ProfilerHttpRequestHandler.getFilter
import com.github.korniloval.flameviewer.ProfilerHttpRequestHandler.sendStatus
import com.github.korniloval.flameviewer.converters.trees.Filter
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

abstract class RequestHandler internal constructor(protected val urlDecoder: QueryStringDecoder,
                                                   protected val context: ChannelHandlerContext) {

    protected val filter: Filter? = getFilter(urlDecoder)
    private val logFile: File? = PluginFileManager.getLogFile(urlDecoder)

    fun process() {
        if (logFile == null) {
            sendStatus(HttpResponseStatus.BAD_REQUEST, context.channel(), "Request does not contain logFile")
            return
        }
        doProcess(logFile)
    }

    protected abstract fun doProcess(logFile: File)
}

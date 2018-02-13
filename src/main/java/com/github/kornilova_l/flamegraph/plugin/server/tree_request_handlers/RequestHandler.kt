package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getFilter
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendStatus
import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter
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

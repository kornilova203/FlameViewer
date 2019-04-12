package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.converters.trees.TreeType.BACK_TRACES
import com.github.korniloval.flameviewer.converters.trees.maximumNodesCount
import com.github.korniloval.flameviewer.server.ServerUtil.*
import com.github.korniloval.flameviewer.server.TreeManager
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File


class BackTracesHandler(treeManager: TreeManager, logger: FlameLogger, findFile: FindFile)
    : AccumulativeTreeHandler(treeManager, logger, BACK_TRACES, findFile) {

    override fun doProcess(ctx: ChannelHandlerContext, file: File, decoder: QueryStringDecoder) {
        val filter = getFilter(decoder, logger)
        val callTraces = treeManager.getTree(file, TreeType.CALL_TRACES, filter)
        if (callTraces == null) {
            sendProto(ctx, null, logger)
            return
        }
        /* if it is a request for all backtraces and calltraces tree contains more than maximumNodesCount then
         * send response BAD_REQUEST */
        if (getParameter(decoder, "method") == null &&
                callTraces.treeInfo.nodesCount > maximumNodesCount) {
            sendStatus(HttpResponseStatus.BAD_REQUEST, ctx.channel(),
                    "Calltraces tree contains too many nodes: ${callTraces.treeInfo.nodesCount}. For this request it must" +
                            "contain less than $maximumNodesCount. For this tree only method backtraces are available.")
        } else {
            super.doProcess(ctx, file, decoder)
        }
    }
}

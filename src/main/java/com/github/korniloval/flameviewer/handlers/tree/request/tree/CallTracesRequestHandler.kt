package com.github.korniloval.flameviewer.handlers.tree.request.tree

import com.github.korniloval.flameviewer.trees.TreeManager
import com.github.korniloval.flameviewer.trees.TreeManager.TreeType.CALL_TRACES
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder

class CallTracesRequestHandler(urlDecoder: QueryStringDecoder, context: ChannelHandlerContext) :
        AccumulativeTreeRequestHandler(urlDecoder, context) {
    override val type: TreeManager.TreeType = CALL_TRACES
}

package com.github.korniloval.flameviewer.handlers.tree.request.tree

import com.github.korniloval.flameviewer.TreeManager
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.converters.trees.TreeType.CALL_TRACES
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder

class CallTracesRequestHandler(urlDecoder: QueryStringDecoder, context: ChannelHandlerContext, treeManager: TreeManager) :
        AccumulativeTreeRequestHandler(urlDecoder, context, treeManager) {
    override val type: TreeType = CALL_TRACES
}

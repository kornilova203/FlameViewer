package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType.OUTGOING_CALLS
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder

class CallTracesRequestHandler(urlDecoder: QueryStringDecoder, context: ChannelHandlerContext) :
        AccumulativeTreeRequestHandler(urlDecoder, context) {
    override val type: TreeManager.TreeType = OUTGOING_CALLS
}

package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers.tree

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.QueryStringDecoder


class BackTracesRequestHandler(urlDecoder: QueryStringDecoder, context: ChannelHandlerContext) :
        AccumulativeTreeRequestHandler(urlDecoder, context) {
    override val type: TreeType = TreeType.BACK_TRACES
}

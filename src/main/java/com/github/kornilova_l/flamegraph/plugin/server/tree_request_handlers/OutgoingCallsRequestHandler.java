package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;

public class OutgoingCallsRequestHandler extends AccumulativeTreeRequestHandler {
    public OutgoingCallsRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context, TreeManager.TreeType.OUTGOING_CALLS);
    }
}

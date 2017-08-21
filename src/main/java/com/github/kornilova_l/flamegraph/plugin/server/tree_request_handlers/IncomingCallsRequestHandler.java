package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;


public class IncomingCallsRequestHandler extends AccumulativeTreeRequestHandler {
    public IncomingCallsRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context, TreeType.INCOMING_CALLS);
    }
}

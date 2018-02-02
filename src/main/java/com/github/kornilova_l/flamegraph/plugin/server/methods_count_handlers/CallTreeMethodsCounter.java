package com.github.kornilova_l.flamegraph.plugin.server.methods_count_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.Nullable;

public class CallTreeMethodsCounter extends MethodsCounter {
    public CallTreeMethodsCounter(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context);
    }

    @Override
    @Nullable
    protected Trees getTrees() {
        return TreeManager.INSTANCE.getCallTree(logFile, null, null);
    }

}

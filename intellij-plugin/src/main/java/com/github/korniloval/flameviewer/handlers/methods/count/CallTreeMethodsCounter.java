package com.github.korniloval.flameviewer.handlers.methods.count;

import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import com.github.korniloval.flameviewer.TreeManager;
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

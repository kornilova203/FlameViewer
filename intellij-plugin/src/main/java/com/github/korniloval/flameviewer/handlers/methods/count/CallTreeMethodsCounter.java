package com.github.korniloval.flameviewer.handlers.methods.count;

import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import com.github.korniloval.flameviewer.TreeManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CallTreeMethodsCounter extends MethodsCounter {
    private final TreeManager treeManager;

    public CallTreeMethodsCounter(QueryStringDecoder urlDecoder, ChannelHandlerContext context, @NotNull TreeManager treeManager) {
        super(urlDecoder, context);
        this.treeManager = treeManager;
    }

    @Override
    @Nullable
    protected Trees getTrees() {
        if (file == null) return null;
        return treeManager.getCallTree(file, null, null);
    }

}

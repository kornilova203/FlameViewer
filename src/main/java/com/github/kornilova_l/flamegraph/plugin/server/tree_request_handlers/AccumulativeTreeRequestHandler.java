package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;
import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto;

public abstract class AccumulativeTreeRequestHandler extends TreeRequestHandler {

    private final TreeType treeType;

    AccumulativeTreeRequestHandler(QueryStringDecoder urlDecoder,
                                   ChannelHandlerContext context,
                                   TreeType treeType) {
        super(urlDecoder, context);
        this.treeType = treeType;
    }

    @Override
    public void process() {
        sendProto(context, getFilteredTree());
    }

    @Nullable
    private Tree getFilteredTree() {
        String methodName = getParameter(urlDecoder, "method");
        String className = getParameter(urlDecoder, "class");
        String desc = getParameter(urlDecoder, "desc");
        String isStaticString = getParameter(urlDecoder, "isStatic");
        if (methodName != null) {
            className = className != null ? className : "";
            desc = desc != null ? desc : "";
            boolean isStatic = isStaticString != null && Objects.equals(isStaticString, "true");
            return TreeManager.getInstance().getTree(logFile, treeType, className, methodName,
                    desc, isStatic, filter);
        } else {
            return TreeManager.getInstance().getTree(logFile, treeType, filter);
        }
    }
}

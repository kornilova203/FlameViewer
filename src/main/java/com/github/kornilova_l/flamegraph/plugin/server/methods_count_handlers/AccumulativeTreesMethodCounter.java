package com.github.kornilova_l.flamegraph.plugin.server.methods_count_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;

public class AccumulativeTreesMethodCounter extends MethodsCounter {
    private final TreeType treeType;

    public AccumulativeTreesMethodCounter(QueryStringDecoder urlDecoder,
                                          ChannelHandlerContext context,
                                          @NotNull TreeType treeType) {
        super(urlDecoder, context);
        this.treeType = treeType;
    }

    @Nullable
    @Override
    protected Trees getTrees() {
        Tree tree = getTree();
        if (tree == null) {
            return null;
        }
        return Trees.newBuilder().addTrees(tree).build();
    }

    public Tree getTree() {
        String methodName = getParameter(urlDecoder, "method");
        String className = getParameter(urlDecoder, "class");
        String desc = getParameter(urlDecoder, "desc");
        if (methodName != null && className != null && desc != null) {
            return TreeManager.getInstance().getTree(
                    logFile, treeType, className, methodName, desc, null);
        } else {
            return TreeManager.getInstance().getTree(logFile, treeType, null);
        }
    }
}

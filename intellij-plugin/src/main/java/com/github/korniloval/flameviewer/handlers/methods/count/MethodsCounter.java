package com.github.korniloval.flameviewer.handlers.methods.count;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.github.korniloval.flameviewer.PluginFileManager;
import com.github.korniloval.flameviewer.ProfilerHttpRequestHandler;
import com.github.korniloval.flameviewer.converters.trees.Filter;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.github.korniloval.flameviewer.ProfilerHttpRequestHandler.getFilter;

public abstract class MethodsCounter {
    final File file;
    private final Filter filter;
    final QueryStringDecoder urlDecoder;
    private final Set<String> methods = new HashSet<>();
    private ChannelHandlerContext context;

    MethodsCounter(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        this.file = PluginFileManager.INSTANCE.getLogFile(urlDecoder);
        this.filter = getFilter(urlDecoder);
        this.context = context;
        this.urlDecoder = urlDecoder;
    }

    private void countMethods(TreeProtos.Tree tree) {
        if (filter == null) {
            countMethodsRecursively(tree.getBaseNode());
        } else {
            countMethodsRecursively(tree.getBaseNode(), filter);
        }
    }

    @Nullable
    protected abstract TreesProtos.Trees getTrees();


    private void countMethodsRecursively(TreeProtos.Tree.Node node) {
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            methods.add(child.getNodeInfo().getClassName() + child.getNodeInfo().getMethodName());
            countMethodsRecursively(child);
        }
    }

    public void sendJson() {
        TreesProtos.Trees trees = getTrees();
        if (trees != null) {
            for (TreeProtos.Tree tree : trees.getTreesList()) {
                countMethods(tree);
            }
        }
        int result = methods.size();
        ProfilerHttpRequestHandler.sendJson(context, new Gson().toJson(new NodesCount(result)));
    }

    private void countMethodsRecursively(TreeProtos.Tree.Node node, @NotNull Filter filter) {
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            if (filter.isNodeIncluded(child)) {
                methods.add(child.getNodeInfo().getClassName() + child.getNodeInfo().getMethodName());
            }
            countMethodsRecursively(child, filter);
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    class NodesCount {
        private int nodesCount;

        NodesCount(int nodesCount) {
            this.nodesCount = nodesCount;
        }
    }
}

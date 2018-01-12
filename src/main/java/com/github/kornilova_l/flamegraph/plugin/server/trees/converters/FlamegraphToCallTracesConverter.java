package com.github.kornilova_l.flamegraph.plugin.server.trees.converters;

import com.github.kornilova_l.flamegraph.plugin.server.trees.FileToCallTracesConverter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees.SimpleStacksOCTreeBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees.StacksOCTreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.converters.jmc.JMCConverter.getBytes;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees.StacksParser.*;

public class FlamegraphToCallTracesConverter extends FileToCallTracesConverter {
    private Logger LOG = Logger.getInstance(FlamegraphToCallTracesConverter.class);

    @NotNull
    public String getId() {
        return "flamegraph";
    }

    public boolean isSupported(@NotNull File file) {
        return isFlamegraph(getBytes(file));
    }

    @NotNull
    public TreeProtos.Tree convert(@NotNull File file) {
        Map<String, Integer> stacks = getStacks(file);
        if (stacks == null) {
            LOG.error("Cannot get stacks from file");
            return TreeProtos.Tree.newBuilder().build();
        }
        TreeProtos.Tree tree;
        if (doCallsContainParameters(stacks)) {
            tree = new StacksOCTreeBuilder(stacks).getTree();
        } else {
            tree = new SimpleStacksOCTreeBuilder(stacks).getTree();
        }
        if (tree == null) {
            LOG.error("Cannot construct tree. File: " + file);
            return TreeProtos.Tree.newBuilder().build();
        }
        return tree;
    }
}
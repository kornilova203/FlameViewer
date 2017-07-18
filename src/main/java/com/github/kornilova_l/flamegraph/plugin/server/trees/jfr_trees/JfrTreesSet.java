package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

public class JfrTreesSet extends TreesSet {
    public JfrTreesSet(File logFile) {
        super(logFile);
        Map<String, Integer> stacks = new FlightRecorderConverter(logFile).getStacks();
        outgoingCalls = new StacksOCTreeBuilder(stacks).getTree();
    }

    @Override
    protected void validateExtension() {
        if (ProfilerHttpRequestHandler.getExtension(logFile.getName()) != TreeManager.Extension.JFR) {
            throw new IllegalArgumentException("Type is not .jfr");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        switch (treeType) {
            case INCOMING_CALLS:
                return null;
            case OUTGOING_CALLS:
                return outgoingCalls;
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType, String className, String methodName, String desc, boolean isStatic) {
        return null;
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree() {
        return null;
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public abstract class TreesSet {
    protected final File logFile;
    protected final HashMap<String, TreeProtos.Tree> methodOutgoingCalls = new HashMap<>();
    protected final HashMap<String, TreeProtos.Tree> methodIncomingCalls = new HashMap<>();
    @Nullable
    protected TreesProtos.Trees callTree;
    @Nullable
    protected TreeProtos.Tree outgoingCalls;
    @Nullable
    protected TreeProtos.Tree incomingCalls;

    public TreesSet(File logFile) {
        this.logFile = logFile;
        validateExtension();
    }

    protected abstract void validateExtension();

    public abstract TreeProtos.Tree getTree(TreeManager.TreeType treeType);

    public final TreeProtos.Tree getTree(TreeManager.TreeType treeType, String className, String methodName, String desc, boolean isStatic) {
        switch (treeType) {
            case OUTGOING_CALLS:
                getTree(TreeManager.TreeType.OUTGOING_CALLS);
                return getTreeForMethod(outgoingCalls, methodOutgoingCalls, className, methodName, desc, isStatic);
            case INCOMING_CALLS:
                getTree(TreeManager.TreeType.INCOMING_CALLS);
                return getTreeForMethod(incomingCalls, methodIncomingCalls, className, methodName, desc, isStatic);
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    public abstract TreesProtos.Trees getCallTree();

    @Nullable
    private static TreeProtos.Tree getTreeForMethod(TreeProtos.Tree sourceTree,
                                                      HashMap<String, TreeProtos.Tree> map,
                                                      String className,
                                                      String methodName,
                                                      String desc,
                                                      boolean isStatic) {
        if (sourceTree == null) {
            return null;
        }
        return map.computeIfAbsent(
                className + methodName + desc,
                n -> new MethodAccumulativeTreeBuilder(
                        sourceTree,
                        className,
                        methodName,
                        desc,
                        isStatic
                ).getTree()
        );
    }
}

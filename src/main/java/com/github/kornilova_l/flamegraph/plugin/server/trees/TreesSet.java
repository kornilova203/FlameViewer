package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
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

    public abstract TreeProtos.Tree getTree(TreeManager.TreeType treeType,
                                            String className,
                                            String methodName,
                                            String desc,
                                            boolean isStatic);

    public abstract TreesProtos.Trees getCallTree();

    protected static TreeProtos.Tree getTreeForMethod(@NotNull TreeProtos.Tree sourceTree,
                                                      HashMap<String, TreeProtos.Tree> map,
                                                      String className,
                                                      String methodName,
                                                      String desc,
                                                      boolean isStatic) {
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

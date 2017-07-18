package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class SerTreeManager implements TreeManager {
    private static final Logger LOG = Logger.getInstance(SerTreeManager.class);
    private File logFile = null;
    @Nullable
    private TreesProtos.Trees originalTrees;
    @Nullable
    private TreeProtos.Tree outgoingCalls;
    private final HashMap<String, TreeProtos.Tree> methodOutgoingCalls = new HashMap<>();
    @Nullable
    private TreeProtos.Tree incomingCalls;
    private final HashMap<String, TreeProtos.Tree> methodIncomingCalls = new HashMap<>();

    public SerTreeManager() {
    }

    private void updateLogFile(File logFile) {
        if (this.logFile == null ||
                !Objects.equals(this.logFile.getAbsolutePath(), logFile.getAbsolutePath())) {
            this.logFile = logFile;
            removeTrees();
        }
    }

    private void removeTrees() {
        originalTrees = null;
        outgoingCalls = null;
        incomingCalls = null;
        methodIncomingCalls.clear();
        methodOutgoingCalls.clear();
    }

    /**
     * Get full tree
     *
     * @return TreeProtos.Tree object. Tree may not have any nodes inside (if all methods took <1ms)
     */
    @Nullable
    private TreeProtos.Tree getOutgoingCalls(File logFile) {
        updateLogFile(logFile);
        if (outgoingCalls == null) {
            getCallTree(logFile);
            if (originalTrees == null) {
                return null;
            }
            outgoingCalls = new OutgoingCallsBuilder(originalTrees).getTree();
        }
        return outgoingCalls;
    }

    @Nullable
    private TreeProtos.Tree getOutgoingCalls(File logFile,
                                             String className,
                                             String methodName,
                                             String desc,
                                             boolean isStatic) {
        updateLogFile(logFile);
        getOutgoingCalls(logFile);
        if (outgoingCalls == null) {
            return null;
        }
        return getTreeForMethod(outgoingCalls, methodOutgoingCalls, className, methodName, desc, isStatic);
    }

    private static TreeProtos.Tree getTreeForMethod(@NotNull TreeProtos.Tree sourceTree,
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

    @Nullable
    private TreeProtos.Tree getIncomingCalls(File logFile) {
        updateLogFile(logFile);
        if (incomingCalls == null) {
            getOutgoingCalls(logFile);
            if (outgoingCalls == null) {
                return null;
            }
            incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
        }
        return incomingCalls;
    }

    @Nullable
    private TreeProtos.Tree getIncomingCalls(File logFile,
                                             String className,
                                             String methodName,
                                             String desc,
                                             boolean isStatic) {
        updateLogFile(logFile);
        getIncomingCalls(logFile);
        if (incomingCalls == null) {
            return null;
        }
        return getTreeForMethod(incomingCalls, methodIncomingCalls, className, methodName, desc, isStatic);
    }

    @Override
    @Nullable
    public TreesProtos.Trees getCallTree(File logFile) {
        updateLogFile(logFile);
        if (originalTrees == null) {
            originalTrees = new CallTreesBuilder(logFile).getTrees();
        }
        return originalTrees;
    }

    @Override
    @Nullable
    public TreeProtos.Tree getTree(File logFile, TreeType treeType) {
        switch (treeType) {
            case INCOMING_CALLS:
                return getIncomingCalls(logFile);
            case OUTGOING_CALLS:
                return getOutgoingCalls(logFile);
        }
        throw new IllegalArgumentException("Tree type is not supported");
    }

    @Override
    @Nullable
    public TreeProtos.Tree getTree(File logFile,
                                   TreeType treeType,
                                   String className,
                                   String methodName,
                                   String desc,
                                   boolean isStatic) {
        switch (treeType) {
            case OUTGOING_CALLS:
                return getOutgoingCalls(logFile, className, methodName, desc, isStatic);
            case INCOMING_CALLS:
                return getIncomingCalls(logFile, className, methodName, desc, isStatic);
        }
        throw new IllegalArgumentException("Tree type is not supported");
    }
}


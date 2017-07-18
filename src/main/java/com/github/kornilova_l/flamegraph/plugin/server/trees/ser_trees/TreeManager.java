package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees.JfrTreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class TreeManager {
    private static final Logger LOG = Logger.getInstance(TreeManager.class);
    private final HashMap<String, SerTreesSet> serTreesSets = new HashMap<>();
    private final HashMap<String, JfrTreesSet> jfrTreesSets = new HashMap<>();

    public TreeManager() {
    }

    @Nullable
    public TreesProtos.Trees getCallTree(File logFile) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = jfrTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getCallTree();
            case SER:
                treesSet = serTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getCallTree();
            case UNSUPPORTED:
            default:
                throw new IllegalArgumentException("Extension is unsupported");
        }
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile, TreeType treeType) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = jfrTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getTree(treeType);
            case SER:
                treesSet = serTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getTree(treeType);
            case UNSUPPORTED:
            default:
                throw new IllegalArgumentException("Extension is unsupported");
        }
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile,
                                   TreeType treeType,
                                   String className,
                                   String methodName,
                                   String desc,
                                   boolean isStatic) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = jfrTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getTree(treeType, className, methodName, desc, isStatic);
            case SER:
                treesSet = serTreesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getTree(treeType, className, methodName, desc, isStatic);
            case UNSUPPORTED:
            default:
                throw new IllegalArgumentException("Extension is unsupported");
        }
    }

    public enum TreeType {
        OUTGOING_CALLS,
        INCOMING_CALLS
    }

    public enum Extension {
        JFR,
        SER,
        UNSUPPORTED
    }
}


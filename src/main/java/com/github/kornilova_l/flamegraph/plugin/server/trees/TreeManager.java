package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees.JfrTreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.SerTreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class TreeManager {
    private static final Logger LOG = Logger.getInstance(TreeManager.class);
    private final HashMap<String, TreesSet> treesSets = new HashMap<>();

    public TreeManager() {
    }

    @Nullable
    public TreesProtos.Trees getCallTree(File logFile, @Nullable Configuration configuration) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getCallTree(configuration);
            case SER:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getCallTree(configuration);
            case UNSUPPORTED:
            default:
                throw new IllegalArgumentException("Extension is unsupported");
        }
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile, TreeType treeType, @Nullable Configuration configuration) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getTree(treeType, configuration);
            case SER:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getTree(treeType, configuration);
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
                                   boolean isStatic,
                                   @Nullable Configuration configuration) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getTree(treeType, className, methodName, desc, isStatic, configuration);
            case SER:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getTree(treeType, className, methodName, desc, isStatic, configuration);
            case UNSUPPORTED:
            default:
                throw new IllegalArgumentException("Extension is unsupported");
        }
    }

    public List<TreesSet.HotSpot> getHotSpots(File logFile) {
        Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
        TreesSet treesSet;
        switch (extension) {
            case JFR:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new JfrTreesSet(logFile));
                return treesSet.getHotSpots();
            case SER:
                treesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                        n -> new SerTreesSet(logFile));
                return treesSet.getHotSpots();
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


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
    private File currentFile = null;
    private TreesSet currentTreesSet = null;

    public TreeManager() {
    }

    @Nullable
    public TreesProtos.Trees getCallTree(File logFile, @Nullable Configuration configuration) {
        updateTreesSet(logFile);

        return currentTreesSet.getCallTree(configuration);

    }

    private void updateTreesSet(File logFile) {
        if (logFile != currentFile) {
            currentFile = logFile;
            Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
            switch (extension) {
                case JFR:
                    currentTreesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                            n -> new JfrTreesSet(logFile));
                    break;
                case SER:
                    currentTreesSet = treesSets.computeIfAbsent(logFile.getAbsolutePath(),
                            n -> new SerTreesSet(logFile));
                    break;
                case UNSUPPORTED:
                default:
                    throw new IllegalArgumentException("Extension is unsupported");
            }
        }
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile, TreeType treeType, @Nullable Configuration configuration) {
        updateTreesSet(logFile);

        return currentTreesSet.getTree(treeType, configuration);
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile,
                                   TreeType treeType,
                                   String className,
                                   String methodName,
                                   String desc,
                                   boolean isStatic,
                                   @Nullable Configuration configuration) {
        updateTreesSet(logFile);
        return currentTreesSet.getTree(treeType, className, methodName, desc, isStatic, configuration);

    }

    public List<TreesSet.HotSpot> getHotSpots(File logFile) {
        updateTreesSet(logFile);
        return currentTreesSet.getHotSpots();
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


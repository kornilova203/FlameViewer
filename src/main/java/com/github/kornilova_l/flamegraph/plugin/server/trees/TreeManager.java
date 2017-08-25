package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees.FlamegraphFormatTreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.SerTreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TreeManager {
    private static final Logger LOG = Logger.getInstance(TreeManager.class);
    private File currentFile = null;
    private volatile TreesSet currentTreesSet = null;
    private AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
    private AtomicBoolean isBusy = new AtomicBoolean(false);
    private static TreeManager treeManager = new TreeManager();

    public static TreeManager getInstance() {
        return treeManager;
    }

    private TreeManager() {
        TreeManager thisTreeManager = this;
        Thread watchLastUpdate = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!thisTreeManager.isBusy() &&
                        thisTreeManager.longTimePassedSinceUpdate()) {
                    thisTreeManager.removeTreesSet();
                }
            }
        });
        watchLastUpdate.setDaemon(true);
        watchLastUpdate.start();
    }

    private boolean isBusy() {
        return isBusy.get();
    }

    private void removeTreesSet() {
        currentTreesSet = null;
        currentFile = null;
        lastUpdate.set(System.currentTimeMillis());
    }

    private boolean longTimePassedSinceUpdate() {
        return System.currentTimeMillis() - lastUpdate.get() >= 30000;
    }


    public TreesProtos.@Nullable Trees getCallTree(File logFile,
                                                   @Nullable Filter filter,
                                                   @Nullable List<Integer> threadsIds) {
        isBusy.set(true);
        updateTreesSet(logFile);

        TreesProtos.Trees callTree = currentTreesSet.getCallTree(filter, threadsIds);

        isBusy.set(false);

        return callTree;

    }

    private void updateTreesSet(File logFile) {
        if (currentFile == null ||
                !Objects.equals(logFile.getAbsolutePath(), currentFile.getAbsolutePath())) {
            currentFile = logFile;
            Extension extension = ProfilerHttpRequestHandler.getExtension(logFile.getName());
            switch (extension) {
                case OTHER:
                case JFR:
                    currentTreesSet = new FlamegraphFormatTreesSet(logFile);
                    break;
                case SER:
                    currentTreesSet = new SerTreesSet(logFile);
                    break;
                default:
                    throw new IllegalArgumentException("Extension is unsupported");
            }
        }
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile, TreeType treeType, @Nullable Filter filter) {
        isBusy.set(true);
        updateTreesSet(logFile);
        TreeProtos.Tree tree = currentTreesSet.getTree(treeType, filter);

        isBusy.set(false);

        return tree;
    }

    @Nullable
    public TreeProtos.Tree getTree(File logFile,
                                   TreeType treeType,
                                   String className,
                                   String methodName,
                                   String desc,
                                   boolean isStatic,
                                   @Nullable Filter filter) {
        isBusy.set(true);
        updateTreesSet(logFile);
        TreeProtos.Tree tree = currentTreesSet.getTree(treeType, className, methodName, desc, isStatic, filter);
        isBusy.set(false);
        return tree;

    }

    public List<TreesSet.HotSpot> getHotSpots(File logFile) {
        isBusy.set(true);
        updateTreesSet(logFile);
        List<TreesSet.HotSpot> hotSpots = currentTreesSet.getHotSpots();
        isBusy.set(false);
        return hotSpots;
    }

    public void updateLastTime() {
        lastUpdate.set(System.currentTimeMillis());
    }

    @Nullable
    public TreesPreview getCallTreesPreview(@Nullable File logFile, Filter filter) {
        isBusy.set(true);
        updateTreesSet(logFile);
        TreesPreview treesPreview = currentTreesSet.getTreesPreview(filter);
        isBusy.set(false);
        return treesPreview;
    }

    public enum TreeType {
        OUTGOING_CALLS,
        INCOMING_CALLS
    }

    public enum Extension {
        OTHER,
        SER,
        JFR
    }
}


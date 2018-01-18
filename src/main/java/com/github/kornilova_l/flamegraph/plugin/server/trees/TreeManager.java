package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.HotSpot;
import com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees.TreesSetImpl;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.SerTreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class TreeManager {
    private static final Logger LOG = Logger.getInstance(PluginFileManager.class);
    private File currentFile = null;
    @Nullable
    private volatile TreesSet currentTreesSet = null;
    private long lastUpdate;
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
                if (thisTreeManager.longTimePassedSinceUpdate()) {
                    thisTreeManager.removeTreesSet();
                }
            }
        });
        watchLastUpdate.setDaemon(true);
        watchLastUpdate.start();
    }

    private void removeTreesSet() {
        currentTreesSet = null;
        currentFile = null;
        lastUpdate = System.currentTimeMillis();
    }

    private synchronized boolean longTimePassedSinceUpdate() {
        return System.currentTimeMillis() - lastUpdate >= 30000;
    }

    @Nullable
    public synchronized TreesProtos.Trees getCallTree(File logFile,
                                                      @Nullable Filter filter,
                                                      @Nullable List<Integer> threadsIds) {
        updateTreesSet(logFile);
        TreesSet currentTreesSet = this.currentTreesSet;
        if (currentTreesSet == null) {
            return null;
        }
        return currentTreesSet.getCallTree(filter, threadsIds);
    }

    private void updateTreesSet(File logFile) {
        if (currentFile == null ||
                !Objects.equals(logFile.getAbsolutePath(), currentFile.getAbsolutePath())) {
            currentFile = logFile;
            if (ProfilerToFlamegraphConverter.Companion.getFileExtension(logFile.getName()).equals("ser")) {
                currentTreesSet = new SerTreesSet(logFile);
                return;
            }
            String parentDirName = PluginFileManager.getParentDirName(logFile);
            if (parentDirName == null) {
                LOG.error("Cannot find parent directory of log file");
                return;
            }
            TreeProtos.Tree callTraces = FileToCallTracesConverter.Companion.convert(parentDirName, logFile);
            if (callTraces == null) {
                LOG.error("Cannot convert file " + logFile);
                return;
            }
            currentTreesSet = new TreesSetImpl(callTraces);
        }
    }

    @Nullable
    public synchronized TreeProtos.Tree getTree(File logFile, TreeType treeType, @Nullable Filter filter) {
        updateTreesSet(logFile);
        TreesSet currentTreesSet = this.currentTreesSet;
        if (currentTreesSet == null) {
            return null;
        }
        return currentTreesSet.getTree(treeType, filter);
    }

    @Nullable
    public synchronized TreeProtos.Tree getTree(File logFile,
                                                TreeType treeType,
                                                String className,
                                                String methodName,
                                                String desc,
                                                boolean isStatic,
                                                @Nullable Filter filter) {
        updateTreesSet(logFile);
        TreesSet currentTreesSet = this.currentTreesSet;
        if (currentTreesSet == null) {
            return null;
        }
        return currentTreesSet.getTree(treeType, className, methodName, desc, isStatic, filter);

    }

    @Nullable
    public synchronized List<HotSpot> getHotSpots(File logFile) {
        updateTreesSet(logFile);
        TreesSet currentTreesSet = this.currentTreesSet;
        if (currentTreesSet == null) {
            return null;
        }
        return currentTreesSet.getHotSpots();
    }

    public synchronized void updateLastTime() {
        lastUpdate = System.currentTimeMillis();
    }

    @Nullable
    public synchronized TreesPreview getCallTreesPreview(@Nullable File logFile, Filter filter) {
        updateTreesSet(logFile);
        TreesSet currentTreesSet = this.currentTreesSet;
        if (currentTreesSet == null) {
            return null;
        }
        return currentTreesSet.getTreesPreview(filter);
    }

    public enum TreeType {
        OUTGOING_CALLS,
        INCOMING_CALLS
    }
}


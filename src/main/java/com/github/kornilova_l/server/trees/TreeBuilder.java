package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Objects;

public class TreeBuilder {
    static final Logger LOG = Logger.getInstance(TreeBuilder.class);
    private File logFile;
    private TreesProtos.Trees originalTrees;
    private TreeProtos.Tree outgoingCallsTree;
    private TreeProtos.Tree fullBackwardTree;

    public void updateLogFile() {
        File newFile = ProfilerFileManager.getLatestFile();
        if (newFile == null) {
            throw new AssertionError("No log file found");
        }
        if (logFile == null || !Objects.equals(newFile.getAbsolutePath(), logFile.getAbsolutePath())) {
            logFile = newFile;
            removeTrees();
        }
    }

    private void removeTrees() {
        originalTrees = null;
        outgoingCallsTree = null;
        fullBackwardTree = null;
    }

    /**
     * Get original tree.
     *
     * @return TreesProtos.Trees. Returning Trees object may not hove any Tree objects inside.
     */
    public TreesProtos.Trees getOriginalTrees() {
        updateLogFile();
        if (originalTrees == null) {
            originalTrees = buildOriginalTrees();
        }
        return originalTrees;
    }

    /**
     * Get full tree
     * @return TreeProtos.Tree object. Tree may not have any nodes inside (if all methods took <1ms)
     */
    public TreeProtos.Tree getOutgoingCallsTree() {
        if (outgoingCallsTree == null) {
            outgoingCallsTree = OutgoingCallsTreeBuilder.buildOutgoingCallsTree(getOriginalTrees());
        }
        return outgoingCallsTree;
    }

    private TreesProtos.Trees buildOriginalTrees() {
        LOG.info("Original tree will be built from this file: " + logFile.getAbsolutePath());
        try (InputStream inputStream = new FileInputStream(logFile)) {
            HashMap<Long, OriginalTreeBuilder> trees = new HashMap<>();
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            long timeOfLastEvent = event.getTime();
            while (event != null) {
                EventProtos.Event finalEvent = event;
                trees.computeIfAbsent(
                        event.getThreadId(),
                        k -> new OriginalTreeBuilder(finalEvent.getTime(), finalEvent.getThreadId())
                ).addEvent(event);
                timeOfLastEvent = event.getTime();
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
            return HashMapToTrees(trees, timeOfLastEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static TreesProtos.Trees HashMapToTrees(HashMap<Long, OriginalTreeBuilder> trees, long timeOfLastEvent) {
        TreesProtos.Trees.Builder treesBuilder = TreesProtos.Trees.newBuilder();
        for (OriginalTreeBuilder originalTreeBuilder : trees.values()) {
            TreeProtos.Tree tree = originalTreeBuilder.getBuiltTree(timeOfLastEvent);
            if (tree != null) {
                treesBuilder.addTrees(
                        tree
                );
            }
        }
        return treesBuilder.build();
    }

    public static void main(String[] args) throws IOException {
        TreeBuilder treeBuilder = new TreeBuilder();
        TreeProtos.Tree fullTree = treeBuilder.getOutgoingCallsTree();
        System.out.println(fullTree.toString());
    }
}


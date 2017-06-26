package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.Objects;

public class TreeBuilder {
    static final Logger LOG = Logger.getInstance(TreeBuilder.class);
    private File logFile;
    private TreesProtos.Trees originalTrees;
    private TreeProtos.Tree outgoingCalls;
    private TreeProtos.Tree fullBackwardTree;

    private void updateLogFile() {
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
        outgoingCalls = null;
        fullBackwardTree = null;
    }

    /**
     * Get original tree.
     *
     * @return TreesProtos.Trees. Returning Trees object may not hove any Tree objects inside.
     */
    public TreesProtos.Trees getCallTree() {
        updateLogFile();
        if (originalTrees == null) {
            originalTrees = CallTreesBuilder.buildOriginalTrees(logFile);
        }
        return originalTrees;
    }

    /**
     * Get full tree
     * @return TreeProtos.Tree object. Tree may not have any nodes inside (if all methods took <1ms)
     */
    public TreeProtos.Tree getOutgoingCalls() {
        updateLogFile();
        if (outgoingCalls == null) {
            outgoingCalls = OutgoingCallsBuilder.buildOutgoingCalls(getCallTree());
        }
        System.out.println("outgoing calls: " + outgoingCalls);
        return outgoingCalls;
    }

    public static void main(String[] args) throws IOException {
        TreeBuilder treeBuilder = new TreeBuilder();
        TreeProtos.Tree outgoingCalls = treeBuilder.getOutgoingCalls();
//        System.out.println(outgoingCalls.toString());
//        TreesProtos.Trees originalTrees = treeBuilder.getCallTree();
//        System.out.println(originalTrees.toString());
    }
}


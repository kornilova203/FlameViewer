package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.server.trees.outgoing_calls.MethodOutgoingCallsBuilder;
import com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsBuilder;
import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TreeBuilder {
    static final Logger LOG = Logger.getInstance(TreeBuilder.class);
    private File logFile;
    private TreesProtos.Trees originalTrees;
    private TreeProtos.Tree outgoingCalls;
    private final HashMap<String, TreeProtos.Tree> methodOutgoingCalls = new HashMap<>();
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

    public TreeProtos.Tree getOutgoingCalls(Map<String, List<String>> parameters) {
        String className = getParamForKey(parameters, "class");
        String methodName = getParamForKey(parameters, "method");
        String desc = getParamForKey(parameters, "desc");
        String isStaticString = getParamForKey(parameters, "isStatic");
        if (methodName == null || className == null || desc == null || isStaticString == null) {
            return null;
        }
        boolean isStatic = Objects.equals(isStaticString, "true");
        return methodOutgoingCalls.computeIfAbsent(
                className + methodName + desc,
                n -> MethodOutgoingCallsBuilder.buildMethodOutgoingCalls(
                        getOutgoingCalls(),
                        className,
                        methodName,
                        desc,
                        isStatic
                )
        );
    }

    private static String getParamForKey(Map<String, List<String>> parameters, String key) {
        List<String> classNameParams = parameters.get(key);
        if (classNameParams == null) {
            LOG.error(key + " key not specified");
            return null;
        }
        return classNameParams.get(0);
    }

    public static void main(String[] args) throws IOException {
//        TreeBuilder treeBuilder = new TreeBuilder();
//        TreeProtos.Tree outgoingCalls = treeBuilder.getOutgoingCalls();
//        System.out.println(outgoingCalls.toString());
//        TreesProtos.Trees originalTrees = treeBuilder.getCallTree();
//        System.out.println(originalTrees.toString());
    }
}


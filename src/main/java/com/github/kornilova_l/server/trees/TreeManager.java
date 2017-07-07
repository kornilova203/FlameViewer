package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.server.trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.server.trees.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.server.trees.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TreeManager {
    private static final Logger LOG = Logger.getInstance(TreeManager.class);
    private File logFile;
    private String fileName = "";
    @Nullable private TreesProtos.Trees originalTrees;
    @Nullable private TreeProtos.Tree outgoingCalls;
    private final HashMap<String, TreeProtos.Tree> methodOutgoingCalls = new HashMap<>();
    @Nullable private TreeProtos.Tree incomingCalls;
    private final HashMap<String, TreeProtos.Tree> methodIncomingCalls = new HashMap<>();

    private void updateLogFile(String fileName) {
        if (!Objects.equals(this.fileName, fileName)) {
            this.fileName = fileName;
            removeTrees();
            logFile = new File(ProfilerFileManager.getFilePath(fileName));
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
     * Get original tree.
     *
     * @return TreesProtos.Trees. Returning Trees object may not hove any Tree objects inside.
     */
    @Nullable
    public TreesProtos.Trees getCallTree(String fileName) {
        updateLogFile(fileName);
        if (originalTrees == null) {
            originalTrees = new CallTreesBuilder(logFile).getTrees();
        }
        return originalTrees;
    }

    /**
     * Get full tree
     *
     * @return TreeProtos.Tree object. Tree may not have any nodes inside (if all methods took <1ms)
     */
    @Nullable
    public TreeProtos.Tree getOutgoingCalls(String fileName) {
        updateLogFile(fileName);
        if (outgoingCalls == null) {
            getCallTree(fileName);
            if (originalTrees == null) {
                return null;
            }
            outgoingCalls = new OutgoingCallsBuilder(originalTrees).getTree();
        }
        return outgoingCalls;
    }

    @Nullable
    public TreeProtos.Tree getOutgoingCalls(Map<String, List<String>> parameters) {
        updateLogFile(fileName);
        getOutgoingCalls(parameters.get("file").get(0));
        if (outgoingCalls == null) {
            return null;
        }
        return getTreeForMethod(parameters, outgoingCalls, methodOutgoingCalls);
    }

    @Nullable
    private static TreeProtos.Tree getTreeForMethod(Map<String, List<String>> parameters,
                                                    @NotNull TreeProtos.Tree sourceTree,
                                                    HashMap<String, TreeProtos.Tree> map) {
        String className = getParamForKey(parameters, "class");
        String methodName = getParamForKey(parameters, "method");
        String desc = getParamForKey(parameters, "desc");
        String isStaticString = getParamForKey(parameters, "isStatic");
        if (methodName == null || className == null || desc == null || isStaticString == null) {
            return null;
        }
        boolean isStatic = Objects.equals(isStaticString, "true");
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
    public TreeProtos.Tree getIncomingCalls(String fileName) {
        updateLogFile(fileName);
        if (incomingCalls == null) {
            getOutgoingCalls(fileName);
            if (outgoingCalls == null) {
                return null;
            }
            incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
        }
        return incomingCalls;
    }

    @Nullable
    public TreeProtos.Tree getIncomingCalls(Map<String, List<String>> parameters) {
        String fileName = parameters.get("file").get(0);
        updateLogFile(fileName);
        getIncomingCalls(fileName);
        if (incomingCalls == null) {
            return null;
        }
        return getTreeForMethod(parameters, incomingCalls, methodIncomingCalls);
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
//        TreeManager treeManager = new TreeManager();
//        treeManager.getIncomingCalls();
//        TreeManager treeManager = new TreeManager();
//        TreeProtos.Tree outgoingCalls = treeManager.getOutgoingCalls();
//        System.out.println(outgoingCalls.toString());
//        TreesProtos.Trees originalTrees = treeManager.getCallTree();
//        System.out.println(originalTrees.toString());
    }
}


package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class SerTreesSet extends TreesSet {
    SerTreesSet(File logFile) {
        super(logFile);
        callTree = new CallTreesBuilder(logFile).getTrees();
    }

    @Override
    protected void validateExtension() {
        if (ProfilerHttpRequestHandler.getExtension(logFile.getName()) != TreeManager.Extension.SER) {
            throw new IllegalArgumentException("Type is not .ser");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        if (callTree == null) {
            return null;
        }
        switch (treeType) {
            case OUTGOING_CALLS:
                if (outgoingCalls == null) {
                    outgoingCalls = new OutgoingCallsBuilder(callTree).getTree();
                }
                return outgoingCalls;
            case INCOMING_CALLS:
                if (incomingCalls == null) {
                    if (outgoingCalls == null) {
                        outgoingCalls = new OutgoingCallsBuilder(callTree).getTree();
                    }
                    if (outgoingCalls == null) {
                        return null;
                    }
                    incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
                }
                return incomingCalls;
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType, String className, String methodName, String desc, boolean isStatic) {
        switch (treeType) {
            case OUTGOING_CALLS:
                getTree(TreeManager.TreeType.OUTGOING_CALLS);
                return methodOutgoingCalls.computeIfAbsent(className + methodName + desc,
                        n -> new MethodAccumulativeTreeBuilder(outgoingCalls, className, methodName, desc, isStatic).getTree());
            case INCOMING_CALLS:
                getTree(TreeManager.TreeType.INCOMING_CALLS);
                return methodIncomingCalls.computeIfAbsent(className + methodName + desc,
                        n -> new MethodAccumulativeTreeBuilder(incomingCalls, className, methodName, desc, isStatic).getTree());
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree() {
        return callTree;
    }
}

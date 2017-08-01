package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class SerTreesSet extends TreesSet {
    public SerTreesSet(File logFile) {
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
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType,
                                   @Nullable Configuration configuration) {
        if (callTree == null) {
            return null;
        }
        if (outgoingCalls == null) {
            outgoingCalls = new OutgoingCallsBuilder(callTree).getTree();
        }
        return getTreeMaybeFilter(treeType, configuration);
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree(@Nullable Configuration configuration) {
        if (callTree == null) {
            return null;
        }
        if (configuration == null) {
            return callTree;
        }
        TreesProtos.Trees.Builder filteredTrees = TreesProtos.Trees.newBuilder();
        for (TreeProtos.Tree tree : callTree.getTreesList()) {
            filteredTrees.addTrees(filterTree(tree, configuration, true));
        }
        return filteredTrees.build();
    }
}

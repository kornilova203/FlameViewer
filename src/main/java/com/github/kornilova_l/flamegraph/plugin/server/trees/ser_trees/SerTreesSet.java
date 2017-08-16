package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreePreviewBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

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
    public TreesPreview getTreesPreview(@Nullable Configuration configuration) {
        Trees callTree = getCallTree(configuration);
        if (callTree == null) {
            return null;
        }
        return new TreePreviewBuilder(callTree).getTreesPreview();
    }

    @Nullable
    @Override
    public Tree getTree(TreeType treeType,
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
    public Trees getCallTree(@Nullable Configuration configuration) {
        if (callTree == null) {
            return null;
        }
        if (configuration == null) {
            return callTree;
        }
        Trees.Builder filteredTrees = Trees.newBuilder();
        for (Tree tree : callTree.getTreesList()) {
            filteredTrees.addTrees(filterTree(tree, configuration, true));
        }
        return filteredTrees.build();
    }

    @Override
    @Nullable
    public Trees getCallTree(@Nullable Configuration configuration, @NotNull List<Integer> threadsIds) {
        Trees trees = getCallTree(configuration);
        if (trees == null) {
            return null;
        }
        Trees.Builder wantedTrees = Trees.newBuilder();
        for (Integer threadsId : threadsIds) {
            wantedTrees.addTrees(trees.getTrees(threadsId));
        }
        return wantedTrees.build();
    }
}

package com.github.kornilova203.flameviewer.converters.trees.calltraces;

import com.github.kornilova203.flameviewer.converters.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.github.kornilova203.flameviewer.converters.trees.TreesUtilKt.*;
import static com.github.kornilova203.flameviewer.server.handlers.CoreUtilKt.treeBuilder;

public final class CallTreeToCallTracesConverter implements TreeBuilder {
    private Tree.Builder treeBuilder;
    @Nullable
    private final Tree tree;
    private int maxDepth = 0;

    public CallTreeToCallTracesConverter(@NotNull TreesProtos.Trees callTrees) {
        initTreeBuilder();
        for (Tree callTree : callTrees.getTreesList()) {
            addTree(treeBuilder.getBaseNodeBuilder(), callTree.getBaseNode());
        }
        if (treeBuilder.getBaseNode().getNodesCount() == 0) {
            tree = null;
            return;
        }
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setNodesIndices(treeBuilder.getBaseNodeBuilder());
        setTreeWidth(treeBuilder);
        setNodesCount(treeBuilder);
        treeBuilder.getTreeInfoBuilder().setTimePercent(1f);
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    @Nullable
    public Tree getTree() {
        return tree;
    }

    private void initTreeBuilder() {
        treeBuilder = treeBuilder(Tree.Node.newBuilder());
    }

    private void addTree(Tree.Node.Builder baseNodeInOC,
                         Tree.Node baseNodeInCT) {
        for (Tree.Node childNodeInCT : baseNodeInCT.getNodesList()) {
            addNodesRecursively(baseNodeInOC, childNodeInCT, 0);
        }
    }

    private void addNodesRecursively(Tree.Node.Builder nodeBuilder, // where to append child
                                     Tree.Node node, // from where get method and it's width
                                     int depth) {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        nodeBuilder = updateNodeList(nodeBuilder, node);
        for (Tree.Node childNode : node.getNodesList()) {
            addNodesRecursively(nodeBuilder, childNode, depth);
        }
    }
}

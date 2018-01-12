package com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.setNodesOffsetRecursively;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.updateNodeList;

/**
 * Builds tree in which methods does not have
 * return value and parameters
 */
public class SimpleStacksOCTreeBuilder implements TreeBuilder {
    final Tree.Builder treeBuilder = Tree.newBuilder();
    private final Tree tree;
    int maxDepth = 0;

    public SimpleStacksOCTreeBuilder(@NotNull Map<String, Integer> stacks) {
        tree = buildTree(stacks);
    }

    protected Tree buildTree(@NotNull Map<String, Integer> stacks) {
        treeBuilder.setBaseNode(Tree.Node.newBuilder());
        processStacks(stacks);
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    void processStacks(Map<String, Integer> stacks) {
        for (Map.Entry<String, Integer> stack : stacks.entrySet()) {
            addStackToTree(stack);
        }
    }

    private void addStackToTree(Map.Entry<String, Integer> stack) {
        int width = stack.getValue();
        String[] calls = stack.getKey().split(";");
        if (calls.length > maxDepth) {
            maxDepth = calls.length;
        }
        Tree.Node.Builder nodeBuilder = treeBuilder.getBaseNodeBuilder();
        for (String call : calls) {
            Tree.Node.NodeInfo.Builder nodeInfo = formNodeInfo(call);
            Tree.Node callNode = Tree.Node.newBuilder()
                    .setWidth(width)
                    .setNodeInfo(nodeInfo)
                    .build();
            nodeBuilder = updateNodeList(nodeBuilder, callNode, -1);
        }
    }

    TreeProtos.Tree.Node.NodeInfo.Builder formNodeInfo(String call) {
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setMethodName(call);
    }

    @Nullable
    @Override
    public Tree getTree() {
        return tree;
    }
}

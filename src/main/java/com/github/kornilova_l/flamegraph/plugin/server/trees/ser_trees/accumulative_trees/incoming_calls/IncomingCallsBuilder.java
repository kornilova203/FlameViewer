package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.incoming_calls;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.AccumulativeTreesHelper.setNodesOffsetRecursively;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.AccumulativeTreesHelper.updateNodeList;

public final class IncomingCallsBuilder implements TreeBuilder {
    private TreeProtos.Tree.Builder treeBuilder;
    @Nullable private final TreeProtos.Tree tree;
    private int maxDepth = 0;

    public IncomingCallsBuilder(@NotNull TreeProtos.Tree outgoingCalls) {
        initTreeBuilder();
        for (TreeProtos.Tree.Node node : outgoingCalls.getBaseNode().getNodesList()) {
            traverseTree(node, 0);
        }
        if (treeBuilder.getBaseNode().getNodesCount() == 0) {
            tree = null;
            return;
        }
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    @Nullable
    public TreeProtos.Tree getTree() {
        return tree;
    }

    private void initTreeBuilder() {
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(TreeProtos.Tree.Node.newBuilder());
    }

    private static class NodeBuilderAndTime {
        TreeProtos.Tree.Node.Builder nodeBuilder;
        long time;

        NodeBuilderAndTime(TreeProtos.Tree.Node.Builder nodeBuilder,
                           long time) {
            this.time = time;
            this.nodeBuilder = nodeBuilder;
        }
    }

    private List<NodeBuilderAndTime> traverseTree(TreeProtos.Tree.Node node, int depth) {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        if (node.getNodesCount() == 0) { // leaf
            return addLeafToBaseNodeChildren(node);
        } else {
            ArrayList<NodeBuilderAndTime> arrayList = new ArrayList<>();
            for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
                for (NodeBuilderAndTime returnedNodeBuilder : traverseTree(childNode, depth)) {
                    long time = returnedNodeBuilder.time;
                    TreeProtos.Tree.Node.Builder childOfReturnedNode =
                            updateNodeList(returnedNodeBuilder.nodeBuilder, node, time);
                    arrayList.add(
                            new NodeBuilderAndTime(
                                    childOfReturnedNode,
                                    time
                            )
                    );
                }
            }
            return arrayList;
        }
    }

    private List<NodeBuilderAndTime> addLeafToBaseNodeChildren(TreeProtos.Tree.Node node) {
        ArrayList<NodeBuilderAndTime> arrayList = new ArrayList<>();
        TreeProtos.Tree.Node.Builder newNode =
                updateNodeList(treeBuilder.getBaseNodeBuilder(), node, -1);
        arrayList.add(
                new NodeBuilderAndTime(
                        newNode,
                        node.getWidth()
                )
        );
        return arrayList;
    }
}

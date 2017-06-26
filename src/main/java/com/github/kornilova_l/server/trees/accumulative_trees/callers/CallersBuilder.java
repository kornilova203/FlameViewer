package com.github.kornilova_l.server.trees.accumulative_trees.callers;

import com.github.kornilova_l.protos.TreeProtos;

import java.util.ArrayList;
import java.util.List;

import static com.github.kornilova_l.server.trees.accumulative_trees.AccumulativeTreesHelper.setTreeWidth;
import static com.github.kornilova_l.server.trees.accumulative_trees.AccumulativeTreesHelper.updateNodeList;

public class CallersBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
    private static int maxDepth = 200;

    public static TreeProtos.Tree buildCallers(TreeProtos.Tree outgoingCalls) {
        initTreeBuilder();
        for (TreeProtos.Tree.Node node : outgoingCalls.getBaseNode().getNodesList()) {
            traverseTree(node);
        }
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    private static void initTreeBuilder() {
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(TreeProtos.Tree.Node.newBuilder());
    }

    private static List<TreeProtos.Tree.Node.Builder> traverseTree(TreeProtos.Tree.Node node) {
        ArrayList<TreeProtos.Tree.Node.Builder> arrayList = new ArrayList<>();
        if (node.getNodesCount() == 0) { // leaf
            arrayList.add(updateNodeList(treeBuilder.getBaseNodeBuilder(), node));
            return arrayList;
        } else {
            for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
                for (TreeProtos.Tree.Node.Builder returnedNodeBuilder : traverseTree(childNode)) {
                    arrayList.add(
                            updateNodeList(returnedNodeBuilder, childNode));
                }
            }
        }
        return arrayList;
    }

    public static TreeProtos.Tree buildCallers(TreeProtos.Tree outgoingCalls,
                                               String className,
                                               String methodName,
                                               String desc,
                                               boolean isStatic) {
        return null;
    }
}

package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.setNodesOffsetRecursively;
import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.setTreeWidth;
import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.updateNodeList;

public class OutgoingCallsBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
    private static int maxDepth = 0;

    public static TreeProtos.Tree buildOutgoingCalls(TreesProtos.Trees callTrees) {
        initTreeBuilder();
        for (TreeProtos.Tree callTree : callTrees.getTreesList()) {
            addTree(treeBuilder.getBaseNodeBuilder(), callTree.getBaseNode());
        }
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    private static void initTreeBuilder() {
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(TreeProtos.Tree.Node.newBuilder());
    }

    private static void addTree(TreeProtos.Tree.Node.Builder baseNodeInOC,
                                TreeProtos.Tree.Node baseNodeInCT) {
        for (TreeProtos.Tree.Node childNodeInCT : baseNodeInCT.getNodesList()) {
            addNodesRecursively(baseNodeInOC, childNodeInCT, 0);
        }
    }

    private static void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeInOC, // where to append child
                                            TreeProtos.Tree.Node nodeInCT, // from where get method and it's width
                                            int depth) {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        nodeInOC = updateNodeList(nodeInOC, nodeInCT);
        for (TreeProtos.Tree.Node childNode : nodeInCT.getNodesList()) {
            addNodesRecursively(nodeInOC, childNode, depth);
        }
    }
}

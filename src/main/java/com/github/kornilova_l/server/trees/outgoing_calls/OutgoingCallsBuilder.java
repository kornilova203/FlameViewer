package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.util.Objects;

public class OutgoingCallsBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
    private static int maxDepth = 0;

    public static TreeProtos.Tree buildOutgoingCalls(TreesProtos.Trees callTrees) {
        initTreeBuilder();
        for (TreeProtos.Tree callTree : callTrees.getTreesList()) {
            addTree(treeBuilder.getBaseNodeBuilder(), callTree.getBaseNode());
        }
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth();
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

    private static void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeInOC,
                                            TreeProtos.Tree.Node nodeInCT,
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

    /**
     * @return Node.Builder from OC which was created or updated
     */
    private static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeInOC,
                                                               TreeProtos.Tree.Node nodeInCT) {
        for (TreeProtos.Tree.Node.Builder childNodeInOC : nodeInOC.getNodesBuilderList()) {
            if (isSameMethod(childNodeInOC, nodeInCT)) {
                addTimeToNode(childNodeInOC, nodeInCT);
                return childNodeInOC;
            }
        }
        return addNodeToList(nodeInOC, nodeInCT);
    }

    private static TreeProtos.Tree.Node.Builder addNodeToList(TreeProtos.Tree.Node.Builder nodeInOC,
                                                              TreeProtos.Tree.Node nodeInCT) {
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(nodeInCT);
        nodeInOC.addNodes(newNodeBuilder); // addNodes copies newNodeBuilder
        return nodeInOC.getNodesBuilder(nodeInOC.getNodesCount() - 1);
    }

    private static TreeProtos.Tree.Node.Builder createNodeBuilder(TreeProtos.Tree.Node nodeInCT) {
        TreeProtos.Tree.Node.NodeInfo CTNodeInfo = nodeInCT.getNodeInfo();
        return TreeProtos.Tree.Node.newBuilder()
                .setNodeInfo(
                        TreeProtos.Tree.Node.NodeInfo.newBuilder()
                                .setClassName(CTNodeInfo.getClassName())
                                .setMethodName(CTNodeInfo.getMethodName())
                                .setDescription(CTNodeInfo.getDescription())
                                .setIsStatic(CTNodeInfo.getIsStatic())
                                .build()
                )
                .setWidth(nodeInCT.getWidth());
    }

    /**
     * Update time in node of full tree
     */
    private static void addTimeToNode(TreeProtos.Tree.Node.Builder nodeInOC,
                                      TreeProtos.Tree.Node nodeInCT) {
        nodeInOC.setWidth(
                nodeInOC.getWidth() + nodeInCT.getWidth()
        );
    }

    /**
     * If class name, method name and description are the same return true
     */
    private static boolean isSameMethod(TreeProtos.Tree.Node.Builder nodeBuilder,
                                        TreeProtos.Tree.Node node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        TreeProtos.Tree.Node.NodeInfo.Builder nodeBuilderInfo = nodeBuilder.getNodeInfoBuilder();
        return Objects.equals(nodeInfo.getClassName(), nodeBuilderInfo.getClassName()) &&
                Objects.equals(nodeInfo.getMethodName(), nodeBuilderInfo.getMethodName()) &&
                Objects.equals(nodeInfo.getDescription(), nodeBuilderInfo.getDescription());
    }

    /**
     * Set offset of nodes in formed tree
     */
    private static void setNodesOffsetRecursively(TreeProtos.Tree.Node.Builder node, long offset) {
        for (TreeProtos.Tree.Node.Builder childNode : node.getNodesBuilderList()) {
            childNode.setOffset(offset);
            setNodesOffsetRecursively(childNode, offset);
            offset += childNode.getWidth();
        }
    }

    private static void setTreeWidth() {
        long treeWidth = 0;
        for (TreeProtos.Tree.Node.Builder node : treeBuilder.getBaseNodeBuilder().getNodesBuilderList()) {
            treeWidth += node.getWidth();
        }
        treeBuilder.setWidth(treeWidth);
    }
}

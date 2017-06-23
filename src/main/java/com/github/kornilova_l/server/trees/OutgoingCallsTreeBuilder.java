package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.util.Objects;

public class OutgoingCallsTreeBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;

    static TreeProtos.Tree buildOutgoingCallsTree(TreesProtos.Trees originalTrees) {
        initTreeBuilder();
        for (TreeProtos.Tree originalTree : originalTrees.getTreesList()) {
            addNodesRecursively(treeBuilder.getBaseNodeBuilder(), originalTree.getBaseNode());
        }
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth();
        return treeBuilder.build();
    }

    private static void initTreeBuilder() {
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(TreeProtos.Tree.Node.newBuilder());
    }

    private static void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeInOCT,
                                            TreeProtos.Tree.Node nodeInOT) {
        nodeInOCT = updateNodeList(nodeInOCT, nodeInOT);
        for (TreeProtos.Tree.Node childNode : nodeInOT.getNodesList()) {
            addNodesRecursively(nodeInOCT, childNode);
        }
    }

    /**
     * @return Node.Builder from OCT which was created or updated
     */
    private static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeInOCT,
                                                               TreeProtos.Tree.Node nodeInOT) {
        for (TreeProtos.Tree.Node.Builder childNodeInOCT : nodeInOCT.getNodesBuilderList()) {
            if (isSameMethod(childNodeInOCT, nodeInOT)) {
                addTimeToNode(childNodeInOCT, nodeInOT);
                return childNodeInOCT;
            }
        }
        return addNodeToList(nodeInOCT, nodeInOT);
    }

    private static TreeProtos.Tree.Node.Builder addNodeToList(TreeProtos.Tree.Node.Builder nodeInOCT,
                                                              TreeProtos.Tree.Node nodeInOT) {
        TreeProtos.Tree.Node.NodeInfo originalNodeInfo = nodeInOT.getNodeInfo();
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(originalNodeInfo);
        nodeInOCT.addNodes(newNodeBuilder);
        return newNodeBuilder;
    }

    private static TreeProtos.Tree.Node.Builder createNodeBuilder(TreeProtos.Tree.Node.NodeInfo originalNodeInfo) {
        return TreeProtos.Tree.Node.newBuilder()
                .setNodeInfo(
                        TreeProtos.Tree.Node.NodeInfo.newBuilder()
                                .setClassName(originalNodeInfo.getClassName())
                                .setMethodName(originalNodeInfo.getMethodName())
                                .setDescription(originalNodeInfo.getDescription())
                                .setIsStatic(originalNodeInfo.getIsStatic())
                                .build()
                );
    }

    /**
     * Update time in node of full tree
     */
    private static void addTimeToNode(TreeProtos.Tree.Node.Builder childNodeBuilder,
                                      TreeProtos.Tree.Node nodeInOriginalTree) {
        childNodeBuilder.setWidth(
                childNodeBuilder.getWidth() + nodeInOriginalTree.getWidth()
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

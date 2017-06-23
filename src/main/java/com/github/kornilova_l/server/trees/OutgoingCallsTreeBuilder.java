package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;

import java.util.List;
import java.util.Objects;

public class OutgoingCallsTreeBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;

    static TreeProtos.Tree buildOutgoingCallsTree(TreesProtos.Trees originalTrees) {
        treeBuilder = TreeProtos.Tree.newBuilder();
        for (TreeProtos.Tree originalTree : originalTrees.getTreesList()) {
            addOriginalTreeToFullTree(originalTree);
        }
        setNodesOffsetRecursively(treeBuilder.getNodesBuilderList(), 0);
        setTreeWidth();
        return treeBuilder.build();
    }

    private static void addOriginalTreeToFullTree(TreeProtos.Tree originalTree) {
        for (TreeProtos.Tree.Node nodeInOT : originalTree.getNodesList()) {
            TreeProtos.Tree.Node.Builder nodeInOCT = addNodeToOCT(null, nodeInOT);
            addNodesRecursively(nodeInOCT, nodeInOT);
        }
    }

    private static void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeInOCT,
                                            TreeProtos.Tree.Node nodeInOT) {
        nodeInOCT = addNodeToOCT(nodeInOCT, nodeInOT);
        for (TreeProtos.Tree.Node childNode : nodeInOT.getNodesList()) {
            addNodesRecursively(nodeInOCT, childNode);
        }
    }

    /**
     * @return Node.Builder from OCT which was created or updated
     */
    private static TreeProtos.Tree.Node.Builder addNodeToOCT(TreeProtos.Tree.Node.Builder nodeInOCT, TreeProtos.Tree.Node nodeInOT) {
        if (nodeInOCT == null) {
            return updateBaseNodeList(nodeInOT);
        } else {
            return updateNodeList(nodeInOCT, nodeInOT);
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

    /**
     * Method is the same as {@link #updateNodeList(TreeProtos.Tree.Node.Builder, TreeProtos.Tree.Node) updateNodeList}
     * but there is no parent node (add to tree nodes)
     */
    private static TreeProtos.Tree.Node.Builder updateBaseNodeList(TreeProtos.Tree.Node nodeInOT) {
        for (TreeProtos.Tree.Node.Builder childNodeInOCT : treeBuilder.getNodesBuilderList()) {
            if (isSameMethod(childNodeInOCT, nodeInOT)) {
                addTimeToNode(childNodeInOCT, nodeInOT);
                return childNodeInOCT;
            }
        }
        return addNodeToBaseList(nodeInOT);
    }

    private static TreeProtos.Tree.Node.Builder addNodeToList(TreeProtos.Tree.Node.Builder nodeInOCT,
                                                              TreeProtos.Tree.Node nodeInOT) {
        TreeProtos.Tree.Node.NodeInfo originalNodeInfo = nodeInOT.getNodeInfo();
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(originalNodeInfo);
        nodeInOCT.addNodes(newNodeBuilder);
        return newNodeBuilder;
    }

    /**
     * Method is the same as {@link #addNodeToList(TreeProtos.Tree.Node.Builder, TreeProtos.Tree.Node) updateNodeList}
     * but there is no parent node (add to tree nodes)
     */
    private static TreeProtos.Tree.Node.Builder addNodeToBaseList(TreeProtos.Tree.Node nodeInOT) {
        TreeProtos.Tree.Node.NodeInfo originalNodeInfo = nodeInOT.getNodeInfo();
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(originalNodeInfo);
        treeBuilder.addNodes(newNodeBuilder);
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
    private static void setNodesOffsetRecursively(List<TreeProtos.Tree.Node.Builder> nodes, long offset) {
        for (TreeProtos.Tree.Node.Builder node : nodes) {
            node.setOffset(offset);
            setNodesOffsetRecursively(node.getNodesBuilderList(), offset);
            offset += node.getWidth();
        }
    }

    private static void setTreeWidth() {
        long treeWidth = 0;
        for (TreeProtos.Tree.Node.Builder node : treeBuilder.getNodesBuilderList()) {
            treeWidth += node.getWidth();
        }
        treeBuilder.setWidth(treeWidth);
    }
}

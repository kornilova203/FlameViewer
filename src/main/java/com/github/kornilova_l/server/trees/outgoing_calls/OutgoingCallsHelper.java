package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;

import java.util.Objects;

class OutgoingCallsHelper {
    static TreeProtos.Tree.Node.NodeInfo.Builder createNodeInfo(String className,
                                                        String methodName,
                                                        String desc,
                                                        boolean isStatic) {
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
                .setIsStatic(isStatic)
                .setCount(1);
    }

    /**
     * If class name, method name and description are the same return true
     */
    static boolean isSameMethod(TreeProtos.Tree.Node.Builder nodeBuilder,
                                        TreeProtos.Tree.Node node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        TreeProtos.Tree.Node.NodeInfo.Builder nodeBuilderInfo = nodeBuilder.getNodeInfoBuilder();
        return Objects.equals(nodeInfo.getClassName(), nodeBuilderInfo.getClassName()) &&
                Objects.equals(nodeInfo.getMethodName(), nodeBuilderInfo.getMethodName()) &&
                Objects.equals(nodeInfo.getDescription(), nodeBuilderInfo.getDescription());
    }

    /**
     * @return Node.Builder from OC which was created or updated
     */
    static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeInOC,
                                                               TreeProtos.Tree.Node nodeInCT) {
        for (TreeProtos.Tree.Node.Builder childNodeInOC : nodeInOC.getNodesBuilderList()) {
            if (isSameMethod(childNodeInOC, nodeInCT)) {
                addTimeToNode(childNodeInOC, nodeInCT);
                return childNodeInOC;
            }
        }
        return addNodeToList(nodeInOC, nodeInCT);
    }

    /**
     * Update time in node of full tree
     */
    private static void addTimeToNode(TreeProtos.Tree.Node.Builder nodeInOC,
                                      TreeProtos.Tree.Node nodeInCT) {
        nodeInOC.setWidth(
                nodeInOC.getWidth() + nodeInCT.getWidth()
        );
        nodeInOC.getNodeInfoBuilder().setCount(
                nodeInOC.getNodeInfoBuilder().getCount() + 1
        );
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
                        OutgoingCallsHelper.createNodeInfo(
                                CTNodeInfo.getClassName(),
                                CTNodeInfo.getMethodName(),
                                CTNodeInfo.getDescription(),
                                CTNodeInfo.getIsStatic()
                        )
                )
                .setWidth(nodeInCT.getWidth());
    }

    /**
     * Set offset of nodes in formed tree
     */
    static void setNodesOffsetRecursively(TreeProtos.Tree.Node.Builder node, long offset) {
        for (TreeProtos.Tree.Node.Builder childNode : node.getNodesBuilderList()) {
            childNode.setOffset(offset);
            setNodesOffsetRecursively(childNode, offset);
            offset += childNode.getWidth();
        }
    }

    static void setTreeWidth(TreeProtos.Tree.Builder treeBuilder) {
        long treeWidth = 0;
        for (TreeProtos.Tree.Node.Builder node : treeBuilder.getBaseNodeBuilder().getNodesBuilderList()) {
            treeWidth += node.getWidth();
        }
        treeBuilder.setWidth(treeWidth);
    }
}

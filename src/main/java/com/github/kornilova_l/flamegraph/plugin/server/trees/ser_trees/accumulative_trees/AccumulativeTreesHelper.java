package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;

import java.util.Objects;

public class AccumulativeTreesHelper {
    public static TreeProtos.Tree.Node.NodeInfo.Builder createNodeInfo(String className,
                                                                       String methodName,
                                                                       String desc,
                                                                       boolean isStatic,
                                                                       int callsCount) {
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
                .setIsStatic(isStatic)
                .setCount(callsCount);
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param node        node in source tree. Information of this node will be added to building tree
     * @return Node.Builder from building tree which was created or updated
     */
    public static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              TreeProtos.Tree.Node node) {
        for (TreeProtos.Tree.Node.Builder childNodeBuilder : nodeBuilder.getNodesBuilderList()) {
            if (isSameMethod(childNodeBuilder, node)) {
                addTimeToNode(childNodeBuilder, node.getWidth());
                int count = node.getNodeInfo().getCount();
                if (count == 0) { // if build outgoing calls from call tree
                    count = 1;
                }
                addCountToNode(childNodeBuilder, count);
                return childNodeBuilder;
            }
        }
        return addNodeToList(nodeBuilder, node, node.getWidth());
    }

    private static void addCountToNode(TreeProtos.Tree.Node.Builder nodeBuilder, int count) {
        nodeBuilder.getNodeInfoBuilder().setCount(
                nodeBuilder.getNodeInfoBuilder().getCount() + count
        );
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param node        node in source tree. Information of this node will be added to building tree
     * @param time        time which will be set (or added) to created or updated node
     * @return Node.Builder from building tree which was created or updated
     */
    public static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              TreeProtos.Tree.Node node,
                                                              long time) {
        for (TreeProtos.Tree.Node.Builder childNodeBuilder : nodeBuilder.getNodesBuilderList()) {
            if (isSameMethod(childNodeBuilder, node)) {
                addTimeToNode(childNodeBuilder, time);
                addCountToNode(childNodeBuilder, node.getNodeInfo().getCount());
                return childNodeBuilder;
            }
        }
        return addNodeToList(nodeBuilder, node, time);
    }

    /**
     * Update time in node of full tree
     */
    private static void addTimeToNode(TreeProtos.Tree.Node.Builder nodeBuilder,
                                      long time) {
        nodeBuilder.setWidth(
                nodeBuilder.getWidth() + time
        );
    }

    private static TreeProtos.Tree.Node.Builder addNodeToList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              TreeProtos.Tree.Node node,
                                                              long time) {
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(node, time);
        nodeBuilder.addNodes(newNodeBuilder);
        return nodeBuilder.getNodesBuilder(nodeBuilder.getNodesCount() - 1);
    }

    private static TreeProtos.Tree.Node.Builder createNodeBuilder(TreeProtos.Tree.Node node,
                                                                  long time) {
        TreeProtos.Tree.Node.NodeInfo CTNodeInfo = node.getNodeInfo();
        int callsCount = node.getNodeInfo().getCount();
        if (callsCount == 0) {
            callsCount = 1;
        }
        return TreeProtos.Tree.Node.newBuilder()
                .setNodeInfo(
                        createNodeInfo(
                                CTNodeInfo.getClassName(),
                                CTNodeInfo.getMethodName(),
                                CTNodeInfo.getDescription(),
                                CTNodeInfo.getIsStatic(),
                                callsCount
                        )
                )
                .setWidth(time);
    }

    /**
     * If class name, method name and description are the same return true
     */
    public static boolean isSameMethod(TreeProtos.Tree.Node.Builder nodeBuilder,
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
    public static void setNodesOffsetRecursively(TreeProtos.Tree.Node.Builder node, long offset) {
        for (TreeProtos.Tree.Node.Builder childNode : node.getNodesBuilderList()) {
            childNode.setOffset(offset);
            setNodesOffsetRecursively(childNode, offset);
            offset += childNode.getWidth();
        }
    }

    public static void setTreeWidth(TreeProtos.Tree.Builder treeBuilder) {
        long treeWidth = 0;
        for (TreeProtos.Tree.Node.Builder node : treeBuilder.getBaseNodeBuilder().getNodesBuilderList()) {
            treeWidth += node.getWidth();
        }
        treeBuilder.setWidth(treeWidth);
    }
}

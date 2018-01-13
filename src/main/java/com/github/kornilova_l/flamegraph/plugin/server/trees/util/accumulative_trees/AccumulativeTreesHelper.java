package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AccumulativeTreesHelper {
    static TreeProtos.Tree.Node.NodeInfo.Builder createNodeInfo(String className,
                                                                String methodName,
                                                                String desc,
                                                                boolean isStatic) {
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
                .setIsStatic(isStatic);
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param node        node in source tree. Information of this node will be added to building tree
     * @return Node.Builder from building tree which was created or updated
     */
    public static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              TreeProtos.Tree.Node node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        return updateNodeList(nodeBuilder, nodeInfo.getClassName(), nodeInfo.getMethodName(),
                nodeInfo.getDescription(), node.getWidth());
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param time        time which will be set (or added) to created or updated node
     *                    (in back traces added time differs for node's time)
     * @return Node.Builder from building tree which was created or updated
     */
    public static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              String className,
                                                              String methodName,
                                                              String description,
                                                              long time) {
        int childCount = nodeBuilder.getNodesCount();
        List<TreeProtos.Tree.Node.Builder> children = nodeBuilder.getNodesBuilderList();
        String comparableName = className + methodName;
        for (int i = 0; i < childCount; i++) {
            TreeProtos.Tree.Node.Builder childNodeBuilder = children.get(i);
            if (isSameMethod(childNodeBuilder, className, methodName, description)) {
                addTimeToNode(childNodeBuilder, time);
                return childNodeBuilder;
            }
            if (comparableName.compareTo(getComparableName(children.get(i))) < 0) { // if insert between
                return addNodeToList(nodeBuilder, className, methodName, description, time, i);
            }
        }
        return addNodeToList(nodeBuilder, className, methodName, description, time, childCount); // no such method and it is biggest
    }

    @NotNull
    private static String getComparableName(TreeProtos.Tree.NodeOrBuilder node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        if (nodeInfo == null) {
            return "";
        }
        return nodeInfo.getClassName() + nodeInfo.getMethodName();
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
                                                              String className,
                                                              String methodName,
                                                              String desc,
                                                              long time,
                                                              int pos) {
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(className, methodName, desc, time);
        nodeBuilder.addNodes(pos, newNodeBuilder);
        return nodeBuilder.getNodesBuilder(pos);
    }

    private static TreeProtos.Tree.Node.Builder createNodeBuilder(String className,
                                                                  String methodName,
                                                                  String desc,
                                                                  long time) {
        return TreeProtos.Tree.Node.newBuilder()
                .setNodeInfo(
                        createNodeInfo(className, methodName, desc, false)
                )
                .setWidth(time);
    }

    /**
     * If class name, method name and description are the same return true
     */
    static boolean isSameMethod(TreeProtos.Tree.Node.Builder nodeBuilder,
                                String className,
                                String methodName,
                                String desc) {
        TreeProtos.Tree.Node.NodeInfo.Builder nodeBuilderInfo = nodeBuilder.getNodeInfoBuilder();
        return Objects.equals(className, nodeBuilderInfo.getClassName()) &&
                Objects.equals(methodName, nodeBuilderInfo.getMethodName()) &&
                Objects.equals(desc, nodeBuilderInfo.getDescription());
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
}

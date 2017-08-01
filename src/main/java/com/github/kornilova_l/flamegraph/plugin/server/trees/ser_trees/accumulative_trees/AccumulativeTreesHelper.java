package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees;

import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AccumulativeTreesHelper {
    static TreeProtos.Tree.Node.NodeInfo.Builder createNodeInfo(String className,
                                                                String methodName,
                                                                String desc,
                                                                boolean isStatic,
                                                                int callsCount,
                                                                EventProtos.Var retVal) {
        TreeProtos.Tree.Node.NodeInfo.Builder nodeInfo = TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
                .setIsStatic(isStatic)
                .setCount(callsCount);
        if (retVal != null) {
            nodeInfo.setReturnValue(retVal);
        }
        return nodeInfo;
    }

    /**
     * @param nodeBuilder node in building tree. Child of this node will be updated or created
     * @param node        node in source tree. Information of this node will be added to building tree
     * @param time        time which will be set (or added) to created or updated node (-1 if time should be taken from node)
     * @param isCallTree
     * @return Node.Builder from building tree which was created or updated
     */
    public static TreeProtos.Tree.Node.Builder updateNodeList(TreeProtos.Tree.Node.Builder nodeBuilder,
                                                              TreeProtos.Tree.Node node,
                                                              long time,
                                                              boolean isCallTree) {
        time = time == -1 ? node.getWidth() : time;
        int childCount = nodeBuilder.getNodesCount();
        List<TreeProtos.Tree.Node.Builder> children = nodeBuilder.getNodesBuilderList();
        String comparableName = getComparableName(node);
        for (int i = 0; i < childCount; i++) {
            TreeProtos.Tree.Node.Builder childNodeBuilder = children.get(i);
            if (isSameMethod(childNodeBuilder, node)) {
                updateNode(childNodeBuilder, node, time);
                return childNodeBuilder;
            }
            if (comparableName.compareTo(getComparableName(children.get(i))) < 0) { // if insert between
                return addNodeToList(nodeBuilder, node, time, i, isCallTree);
            }
        }
        return addNodeToList(nodeBuilder, node, time, childCount, isCallTree); // no such method and it is biggest
    }

    private static void updateNode(TreeProtos.Tree.Node.Builder childNodeBuilder, TreeProtos.Tree.Node node, long time) {
        addTimeToNode(childNodeBuilder, time);
        int count = node.getNodeInfo().getCount();
        count = count == 0 ? 1 : count;
        addCountToNode(childNodeBuilder, count);
    }

    @NotNull
    private static String getComparableName(TreeProtos.Tree.NodeOrBuilder node) {
        TreeProtos.Tree.Node.NodeInfo nodeInfo = node.getNodeInfo();
        if (nodeInfo == null) {
            return "";
        }
        return nodeInfo.getClassName() + nodeInfo.getMethodName();
    }

    private static void addCountToNode(TreeProtos.Tree.Node.Builder nodeBuilder, int count) {
        nodeBuilder.getNodeInfoBuilder().setCount(
                nodeBuilder.getNodeInfoBuilder().getCount() + count
        );
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
                                                              long time,
                                                              int pos,
                                                              boolean isCallTree) {
        TreeProtos.Tree.Node.Builder newNodeBuilder = createNodeBuilder(node, time, isCallTree);
        nodeBuilder.addNodes(pos, newNodeBuilder);
        return nodeBuilder.getNodesBuilder(pos);
    }

    private static TreeProtos.Tree.Node.Builder createNodeBuilder(TreeProtos.Tree.Node node,
                                                                  long time,
                                                                  boolean isCallTree) {
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
                                callsCount,
                                isCallTree ? CTNodeInfo.getReturnValue() : null
                        )
                )
                .setWidth(time);
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

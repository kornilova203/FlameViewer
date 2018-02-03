package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;

public class MethodAccumulativeTreeBuilder implements TreeBuilder {
    private final Tree tree;
    private final String className;
    private final String methodName;
    private final String desc;
    private Tree.Builder treeBuilder;
    private Tree.Node.Builder wantedMethodNode;
    private int maxDepth = 0;

    public MethodAccumulativeTreeBuilder(Tree sourceTree,
                                         String className,
                                         String methodName,
                                         String desc) {
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        initTreeBuilder();
        traverseTreeAndFind(sourceTree.getBaseNode());
        TreesUtil.INSTANCE.setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        TreesUtil.INSTANCE.setTreeWidth(treeBuilder);
        TreesUtil.INSTANCE.setNodesCount(treeBuilder);
        setTimePercent(sourceTree);
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    /**
     * It calculates total time of method (not only self-time)
     */
    private void setTimePercent(Tree sourceTree) {
        treeBuilder.getTreeInfoBuilder().setTimePercent(
                calculateTimeOfMethodRecursively(sourceTree.getBaseNode()) / (float) sourceTree.getWidth()
        );
    }

    private long calculateTimeOfMethodRecursively(Tree.Node node) {
        if (TreesUtil.INSTANCE.isSameMethod(wantedMethodNode, node.getNodeInfo().getClassName(), node.getNodeInfo().getMethodName(),
                node.getNodeInfo().getDescription())) {
            /* do not go deeper. We do not want to add up time of recursive calls */
            return node.getWidth();
        }
        long time = 0;
        for (Tree.Node child : node.getNodesList()) {
            time += calculateTimeOfMethodRecursively(child);
        }
        return time;
    }

    public Tree getTree() {
        return tree;
    }

    private void traverseTreeAndFind(Tree.Node node) {

        if (TreesUtil.INSTANCE.isSameMethod(wantedMethodNode, node.getNodeInfo().getClassName(), node.getNodeInfo().getMethodName(),
                node.getNodeInfo().getDescription())) {
            addNodesRecursively(treeBuilder.getBaseNodeBuilder(), node, 0);
        }
        for (Tree.Node childNode : node.getNodesList()) {
            traverseTreeAndFind(childNode);
        }
    }

    private void addNodesRecursively(Tree.Node.Builder nodeBuilder, // where to append child
                                     Tree.Node node, // from where get method and it's width
                                     int depth) {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        nodeBuilder = TreesUtil.INSTANCE.updateNodeList(nodeBuilder, node);
        for (Tree.Node childNode : node.getNodesList()) {
            addNodesRecursively(nodeBuilder, childNode, depth);
        }
    }

    private void initTreeBuilder() {
        Tree.Node.Builder baseNode = Tree.Node.newBuilder()
                .addNodes(Tree.Node.newBuilder()
                        .setNodeInfo(
                                TreesUtil.INSTANCE.createNodeInfo(
                                        className,
                                        methodName,
                                        desc
                                )
                        ));
        treeBuilder = Tree.newBuilder()
                .setBaseNode(baseNode);
        wantedMethodNode = treeBuilder.getBaseNodeBuilder().getNodesBuilder(0);
    }
}

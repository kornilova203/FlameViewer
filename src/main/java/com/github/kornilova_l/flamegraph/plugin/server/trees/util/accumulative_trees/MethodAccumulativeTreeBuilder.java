package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.*;

public class MethodAccumulativeTreeBuilder implements TreeBuilder {
    private final Tree tree;
    private final String className;
    private final String methodName;
    private final String desc;
    private final boolean isStatic;
    private Tree.Builder treeBuilder;
    private Tree.Node.Builder wantedMethodNode;
    private int maxDepth = 0;

    /**
     * @param outgoingTree is needed to calculate time
     */
    public MethodAccumulativeTreeBuilder(Tree sourceTree,
                                         Tree outgoingTree,
                                         String className,
                                         String methodName,
                                         String desc,
                                         boolean isStatic) {
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
        this.isStatic = isStatic;
        initTreeBuilder();
        traverseTreeAndFind(sourceTree.getBaseNode());
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        setTimePercent(outgoingTree);
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    /**
     * Does not matter is it call traces or back traces,
     * to calculate time call traces are needed.
     * It calculates total time of method (not only self-time)
     */
    private void setTimePercent(Tree outgoingTree) {
        treeBuilder.getTreeInfoBuilder().setTimePercent(
                calculateTimeOfMethodRecursively(outgoingTree.getBaseNode()) / (float) outgoingTree.getWidth()
        );
    }

    private long calculateTimeOfMethodRecursively(Tree.Node node) {
        if (AccumulativeTreesHelper.isSameMethod(wantedMethodNode, node.getNodeInfo().getClassName(), node.getNodeInfo().getMethodName(),
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

        if (AccumulativeTreesHelper.isSameMethod(wantedMethodNode, node.getNodeInfo().getClassName(), node.getNodeInfo().getMethodName(),
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
        nodeBuilder = updateNodeList(nodeBuilder, node);
        for (Tree.Node childNode : node.getNodesList()) {
            addNodesRecursively(nodeBuilder, childNode, depth);
        }
    }

    private void initTreeBuilder() {
        Tree.Node.Builder baseNode = Tree.Node.newBuilder()
                .addNodes(Tree.Node.newBuilder()
                        .setNodeInfo(
                                createNodeInfo(
                                        className,
                                        methodName,
                                        desc,
                                        isStatic
                                )
                        ));
        treeBuilder = Tree.newBuilder()
                .setBaseNode(baseNode);
        wantedMethodNode = treeBuilder.getBaseNodeBuilder().getNodesBuilder(0);
    }
}

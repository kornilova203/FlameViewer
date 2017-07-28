package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.AccumulativeTreesHelper.*;

public class MethodAccumulativeTreeBuilder implements TreeBuilder {
    private TreeProtos.Tree.Builder treeBuilder;
    private final TreeProtos.Tree tree;
    private TreeProtos.Tree.Node.Builder wantedMethodNode;
    private int maxDepth = 0;
    private final String className;
    private final String methodName;
    private final String desc;
    private final boolean isStatic;

    public MethodAccumulativeTreeBuilder(TreeProtos.Tree sourceTree,
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
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    public TreeProtos.Tree getTree() {
        return tree;
    }

    private void traverseTreeAndFind(TreeProtos.Tree.Node node) {

        if (AccumulativeTreesHelper.isSameMethod(wantedMethodNode, node)) {
            addNodesRecursively(treeBuilder.getBaseNodeBuilder(), node, 0);
        }
        for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
            traverseTreeAndFind(childNode);
        }
    }

    private void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeBuilder, // where to append child
                                     TreeProtos.Tree.Node node, // from where get method and it's width
                                     int depth) {
        depth++;
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        nodeBuilder = updateNodeList(nodeBuilder, node, -1);
        for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
            addNodesRecursively(nodeBuilder, childNode, depth);
        }
    }

    private void initTreeBuilder() {
        TreeProtos.Tree.Node.Builder baseNode = TreeProtos.Tree.Node.newBuilder()
                .addNodes(TreeProtos.Tree.Node.newBuilder()
                        .setNodeInfo(
                                createNodeInfo(
                                        className,
                                        methodName,
                                        desc,
                                        isStatic,
                                        0
                                )
                        ));
        wantedMethodNode = baseNode.getNodesBuilder(0);
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(baseNode);
    }
}

package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.server.trees.TreeBuilder;

import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.setNodesOffsetRecursively;
import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.setTreeWidth;
import static com.github.kornilova_l.server.trees.outgoing_calls.OutgoingCallsHelper.updateNodeList;

public class MethodOutgoingCallsBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
    private static TreeProtos.Tree.Node.Builder wantedMethodNode;
    private static int maxDepth = 0;
    private static String className;
    private static String methodName;
    private static String desc;
    private static boolean isStatic;

    public static TreeProtos.Tree buildMethodOutgoingCalls(TreeProtos.Tree outgoingCalls,
                                                           String className,
                                                           String methodName,
                                                           String desc,
                                                           boolean isStatic) {
        MethodOutgoingCallsBuilder.className = className;
        MethodOutgoingCallsBuilder.methodName = methodName;
        MethodOutgoingCallsBuilder.desc = desc;
        MethodOutgoingCallsBuilder.isStatic = isStatic;
        initTreeBuilder();
        traverseTreeAndFind(outgoingCalls.getBaseNode());
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    private static void traverseTreeAndFind(TreeProtos.Tree.Node node) {

        if (OutgoingCallsHelper.isSameMethod(wantedMethodNode, node)) {
            addNodesRecursively(treeBuilder.getBaseNodeBuilder(), node, 0);
        }
        for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
            traverseTreeAndFind(childNode);
        }
    }

    private static void addNodesRecursively(TreeProtos.Tree.Node.Builder nodeInOC, // where to append child
                                            TreeProtos.Tree.Node nodeInCT, // from where get method and it's width
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

    private static void initTreeBuilder() {
        TreeProtos.Tree.Node.Builder baseNode = TreeProtos.Tree.Node.newBuilder()
                .addNodes(TreeProtos.Tree.Node.newBuilder()
                        .setNodeInfo(
                                OutgoingCallsHelper.createNodeInfo(
                                        className,
                                        methodName,
                                        desc,
                                        isStatic
                                )
                        ));
        wantedMethodNode = baseNode.getNodesBuilder(0);
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(baseNode);
    }

    public static void main(String[] args) {
        TreeProtos.Tree tree = buildMethodOutgoingCalls(new TreeBuilder().getOutgoingCalls(),
                "samples/Sample",
                "run",
                "()V",
                false);
        System.out.println("method: " + tree);
    }
}

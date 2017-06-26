package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;

public class MethodOutgoingCallsBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
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
        traverseTreeAndBuild(outgoingCalls.getBaseNode(), 0);
        return treeBuilder.build();
    }

    private static void traverseTreeAndBuild(TreeProtos.Tree.Node node, int depth) {
//        if (node)
//            for (TreeProtos.Tree.Node childNode : node.getNodesList()) {
//
//            }
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
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(baseNode);
    }
}

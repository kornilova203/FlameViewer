package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;

public class MethodOutgoingCallsBuilder {
    private static TreeProtos.Tree.Builder treeBuilder;
    private static int maxDepth = 0;

    public static TreeProtos.Tree buildMethodOutgoingCalls(TreeProtos.Tree outgoingCalls,
                                                           String className,
                                                           String methodName,
                                                           String desc) {
        initTreeBuilder();
//        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
//        setTreeWidth();
//        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    private static void initTreeBuilder() {
        treeBuilder = TreeProtos.Tree.newBuilder()
                .setBaseNode(TreeProtos.Tree.Node.newBuilder());
    }
}

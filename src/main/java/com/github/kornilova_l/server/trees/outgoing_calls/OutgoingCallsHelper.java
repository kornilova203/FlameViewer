package com.github.kornilova_l.server.trees.outgoing_calls;

import com.github.kornilova_l.protos.TreeProtos;

class OutgoingCallsHelper {
    static TreeProtos.Tree.Node.NodeInfo createNodeInfo(String className,
                                                        String methodName,
                                                        String desc,
                                                        boolean isStatic) {
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc)
                .setIsStatic(isStatic)
                .build();
    }
}

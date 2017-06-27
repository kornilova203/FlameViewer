package com.github.kornilova_l.server.trees.accumulative_trees.incoming_calls;

import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.server.trees.TreeBuilderInterface;

public class MethodIncomingCallsBuilder implements TreeBuilderInterface {
    private TreeProtos.Tree tree;

    public MethodIncomingCallsBuilder(TreeProtos.Tree outgoingCalls,
                                                     String className,
                                                     String methodName,
                                                     String desc,
                                                     boolean isStatic) {
    }

    public TreeProtos.Tree getTree() {
        return tree;
    }
}

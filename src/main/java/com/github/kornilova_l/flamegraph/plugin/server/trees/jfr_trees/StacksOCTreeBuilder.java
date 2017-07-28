package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.AccumulativeTreesHelper.*;

public class StacksOCTreeBuilder implements TreeBuilder {
    private final TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    private final TreeProtos.Tree tree;
    private int maxDepth = 0;

    StacksOCTreeBuilder(@NotNull Map<String, Integer> stacks) {
        treeBuilder.setBaseNode(TreeProtos.Tree.Node.newBuilder());
        processStacks(stacks);
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        tree = treeBuilder.build();
    }

    @NotNull
    private static String getRetType(String call) {
        int space = call.indexOf(" ");
        if (space != -1) {
            return call.substring(0, space);
        }
        throw new IllegalArgumentException("Method does not contain return value");
    }

    private void processStacks(Map<String, Integer> stacks) {
        for (Map.Entry<String, Integer> stack : stacks.entrySet()) {
            addStackToTree(stack);
        }
    }

    private void addStackToTree(Map.Entry<String, Integer> stack) {
        int width = stack.getValue();
        String[] calls = stack.getKey().split(";");
        if (calls.length > maxDepth) {
            maxDepth = calls.length;
        }
        TreeProtos.Tree.Node.Builder nodeBuilder = treeBuilder.getBaseNodeBuilder();
        for (String call : calls) {
            String retType = getRetType(call);
            String classAndMethod = call.substring(call.indexOf(" ") + 1, call.indexOf("("));
            String className = getClassName(classAndMethod);
            String methodName = getMethodName(classAndMethod);
            String desc = getDesc(call);
            TreeProtos.Tree.Node callNode = TreeProtos.Tree.Node.newBuilder()
                    .setWidth(width)
                    .setNodeInfo(
                            TreeProtos.Tree.Node.NodeInfo.newBuilder()
                                    .setClassName(className)
                                    .setMethodName(methodName)
                                    .setDescription(desc + retType)
                                    .setIsStatic(false)
                    )
                    .build();
            nodeBuilder = updateNodeList(nodeBuilder, callNode, -1);
        }
    }

    @NotNull
    private static String getDesc(String call) {
        return call.substring(call.indexOf("("), call.indexOf(")") + 1);
    }

    @NotNull
    private static String getMethodName(String classAndMethod) {
        int lastDot = classAndMethod.lastIndexOf(".");
        if (lastDot != -1) {
            return classAndMethod.substring(lastDot + 1, classAndMethod.length());
        }
        throw new IllegalArgumentException("Method does not contain return value");
    }

    @NotNull
    private static String getClassName(String classAndMethod) {
        int lastDot = classAndMethod.lastIndexOf(".");
        if (lastDot != -1) {
            return classAndMethod.substring(0, lastDot);
        }
        throw new IllegalArgumentException("Method does not contain return value");

    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree() {
        return tree;
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.setNodesOffsetRecursively;

/**
 * Builds tree in which methods contain parameters
 * and maybe contain return value
 */
public class StacksOCTreeBuilder extends SimpleStacksOCTreeBuilder {

    StacksOCTreeBuilder(@NotNull Map<String, Integer> stacks) {
        super(stacks);
    }

    @Override
    protected TreeProtos.Tree buildTree(@NotNull Map<String, Integer> stacks) {
        treeBuilder.setBaseNode(TreeProtos.Tree.Node.newBuilder());
        processStacks(stacks);
        setNodesOffsetRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        setTreeWidth(treeBuilder);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    @Override
    TreeProtos.Tree.Node.NodeInfo.Builder formNodeInfo(String call) {
        String retType = getRetType(call);
        String classAndMethod;
        if (!retType.equals("")) { // if contains return value
            classAndMethod = call.substring(retType.length() + 1, call.indexOf("("));
        } else {
            classAndMethod = call.substring(0, call.indexOf("("));
        }
        String className = getClassName(classAndMethod);
        String methodName = getMethodName(classAndMethod);
        String desc = getDesc(call);
        return TreeProtos.Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(desc + retType)
                .setIsStatic(false);
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

    @NotNull
    private static String getRetType(String call) {
        int space = call.indexOf(' ');
        int openBracket = call.indexOf('(');
        if (space != -1 && space < openBracket) { // if space exist and it is not in parameters
            return call.substring(0, space);
        } else {
            return "";
        }
    }
}

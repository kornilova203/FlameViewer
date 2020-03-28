package com.github.kornilova203.flameviewer.server.handlers;

import com.github.kornilova203.flameviewer.converters.trees.Filter;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class MethodsCounter {
    public static int countMethods(@NotNull TreesProtos.Trees trees, @Nullable Filter filter) {
        HashSet<String> methods = new HashSet<>();
        for (TreeProtos.Tree tree : trees.getTreesList()) {
            countMethodsRecursively(tree.getBaseNode(), methods, filter);
        }
        return methods.size();
    }

    public static int countMethods(@NotNull TreeProtos.Tree tree, @Nullable Filter filter) {
        HashSet<String> methods = new HashSet<>();
        countMethodsRecursively(tree.getBaseNode(), methods, filter);
        return methods.size();
    }

    private static void countMethodsRecursively(TreeProtos.Tree.Node node, Set<String> methods, @Nullable Filter filter) {
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            if (filter == null || filter.isIncluded(child)) {
                methods.add(child.getNodeInfo().getClassName() + child.getNodeInfo().getMethodName());
            }
            countMethodsRecursively(child, methods, filter);
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    static class NodesCount {
        private int nodesCount;

        NodesCount(int nodesCount) {
            this.nodesCount = nodesCount;
        }
    }
}

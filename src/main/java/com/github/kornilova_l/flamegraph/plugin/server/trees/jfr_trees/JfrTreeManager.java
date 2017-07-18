package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;

public class JfrTreeManager {
    @NotNull
    public static TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        switch (treeType) {
            case OUTGOING_CALLS:
                return null;
            case INCOMING_CALLS:
                return null;
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }
}

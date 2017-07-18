package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;

import java.io.File;

public interface TreeManager {
    enum TreeType {
        OUTGOING_CALLS,
        INCOMING_CALLS
    }

    TreesProtos.Trees getCallTree(File logFile);

    TreeProtos.Tree getTree(File logFile, TreeType treeType);

    TreeProtos.Tree getTree(File logFile,
                            TreeType treeType,
                            String className,
                            String methodName,
                            String desc,
                            boolean isStatic);
}

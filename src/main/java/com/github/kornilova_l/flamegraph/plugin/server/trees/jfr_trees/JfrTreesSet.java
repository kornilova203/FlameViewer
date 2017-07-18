package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class JfrTreesSet extends TreesSet {
    public JfrTreesSet(File logFile) {
        super(logFile);
    }


    @Override
    protected void validateExtension() {
        if (ProfilerHttpRequestHandler.getExtension(logFile.getName()) != TreeManager.Extension.JFR) {
            throw new IllegalArgumentException("Type is not .jfr");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        return null;
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType, String className, String methodName, String desc, boolean isStatic) {
        return null;
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree() {
        return null;
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph;

import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreesSetImpl extends TreesSet {
    public TreesSetImpl(@NotNull TreeProtos.Tree callTraces) {
        this.callTraces = callTraces;
    }

    @Override
    public TreesPreviewProtos.TreesPreview getTreesPreview(@Nullable Filter filter) {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType, Filter filter) {
        return getTreeMaybeFilter(treeType, filter);
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree(@Nullable Filter filter) {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }

    @Override
    public TreesProtos.Trees getCallTree(@Nullable Filter filter, @Nullable List<Integer> threadsIds) {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }
}

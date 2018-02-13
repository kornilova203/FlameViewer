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
        this.setCallTraces(callTraces);
    }

    @Override
    public TreesPreviewProtos.TreesPreview getTreesPreview(@Nullable Filter filter) {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(@NotNull TreeManager.TreeType treeType, Filter filter) {
        switch (treeType) {
            case BACK_TRACES:
                return getBackTracesMaybeFiltered(filter);
            case CALL_TRACES:
                return getCallTracesMaybeFiltered(filter);
            default:
                throw new IllegalArgumentException("Tree type " + treeType + " is unsupported");
        }
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

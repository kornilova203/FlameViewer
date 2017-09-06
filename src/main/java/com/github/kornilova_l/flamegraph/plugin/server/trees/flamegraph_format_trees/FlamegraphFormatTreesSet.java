package com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.flamegraph_format_trees.StacksParser.getStacks;

public class FlamegraphFormatTreesSet extends TreesSet {
    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(FlamegraphFormatTreesSet.class);

    public FlamegraphFormatTreesSet(File convertedFile) {
        super(convertedFile);
        Map<String, Integer> stacks = getStacks(convertedFile);
        if (stacks == null) {
            outgoingCalls = null;
        } else {
            long startTime = System.currentTimeMillis();
            outgoingCalls = new StacksOCTreeBuilder(stacks).getTree();
            LOG.info("Building outgoing calls for: " + convertedFile.getName() + " took " + (System.currentTimeMillis() - startTime) + "ms");
        }
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

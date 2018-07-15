package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph;

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.FileToCallTracesConverter;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph.StacksParser.getStacks;
import static com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph.StacksParser.isFlamegraph;
import static com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.jmc.JMCConverter.getBytes;

/**
 * Converts file in flamegraph format to call traces.
 *
 * This format does not allow unmerged stacktraces.
 * So if a file has following content:
 * a() 5
 * a() 5
 * Second line will be ignored.
 */
public class FlamegraphToCallTracesConverter extends FileToCallTracesConverter {
    private Logger LOG = Logger.getInstance(FlamegraphToCallTracesConverter.class);

    @NotNull
    public String getId() {
        return "flamegraph";
    }

    public boolean isSupported(@NotNull File file) {
        return isFlamegraph(getBytes(file));
    }

    @NotNull
    public TreeProtos.Tree convert(@NotNull File file) {
        Map<String, Integer> stacks = getStacks(file);
        if (stacks == null) {
            LOG.error("Cannot get stacks from file");
            return TreeProtos.Tree.newBuilder().build();
        }
        TreeProtos.Tree tree;
        tree = new StacksToTreeBuilder(stacks).getTree();
        if (tree == null) {
            LOG.error("Cannot construct tree. File: " + file);
            return TreeProtos.Tree.newBuilder().build();
        }
        return tree;
    }
}
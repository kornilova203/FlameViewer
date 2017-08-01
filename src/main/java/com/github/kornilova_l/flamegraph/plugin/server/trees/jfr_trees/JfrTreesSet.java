package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees.FlightRecorderConverter.getStacks;

public class JfrTreesSet extends TreesSet {
    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(JfrTreesSet.class);

    public JfrTreesSet(File logFile) {
        super(logFile);
        PluginFileManager fileManager = new PluginFileManager(PathManager.getSystemPath());
        File convertedFile = fileManager.getConvertedFile(logFile.getName());
        if (convertedFile == null) {
            long startTime = System.currentTimeMillis();
            convertedFile = fileManager.createdFileForConverted(logFile);
            new FlightRecorderConverter(logFile).writeTo(convertedFile);
            LOG.info("Converting of: " + logFile.getName() + " took " + (System.currentTimeMillis() - startTime) + "ms");
        }
        Map<String, Integer> stacks = getStacks(convertedFile);
        if (stacks == null) {
            outgoingCalls = null;
        } else {
            long startTime = System.currentTimeMillis();
            outgoingCalls = new StacksOCTreeBuilder(stacks).getTree();
            LOG.info("Building outgoing calls for: " + logFile.getName() + " took " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    @Override
    protected void validateExtension() {
        if (ProfilerHttpRequestHandler.getExtension(logFile.getName()) != TreeManager.Extension.JFR) {
            throw new IllegalArgumentException("Type is not .jfr");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType, Configuration configuration) {
        return getTreeMaybeFilter(treeType, configuration);
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree(@Nullable Configuration configuration) {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }
}

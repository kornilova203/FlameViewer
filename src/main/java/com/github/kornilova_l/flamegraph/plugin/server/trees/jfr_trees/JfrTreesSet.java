package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.intellij.openapi.application.PathManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees.FlightRecorderConverter.getStacks;

// TODO: remove package from parameters and return value when building tree

public class JfrTreesSet extends TreesSet {
    public JfrTreesSet(File logFile) {
        super(logFile);
        PluginFileManager fileManager = new PluginFileManager(PathManager.getSystemPath());
        File convertedFile = fileManager.getConvertedFile(logFile.getName());
        if (convertedFile == null) {
            convertedFile = fileManager.createdFileForConverted(logFile);
            new FlightRecorderConverter(logFile).writeTo(convertedFile);
        }
        Map<String, Integer> stacks = getStacks(convertedFile);
        if (stacks == null) {
            outgoingCalls = null;
        } else {
            outgoingCalls = new StacksOCTreeBuilder(stacks).getTree();
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
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        switch (treeType) {
            case INCOMING_CALLS:
                if (outgoingCalls == null) {
                    return null;
                }
                if (incomingCalls == null) {
                    incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
                }
                return incomingCalls;
            case OUTGOING_CALLS:
                return outgoingCalls;
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree() {
        throw new UnsupportedOperationException("Call tree is not supported for .jfr");
    }

    @NotNull
    @Override
    protected String getBeautifulRetVal(String description) {
        return removePackage(
                description.substring(description.indexOf(")") + 1, description.length())
        );
    }

    @NotNull
    @Override
    protected List<String> getBeautifulParams(String desc) {
        String innerPart = desc.substring(1, desc.indexOf(")"));
        String[] stringParameters = innerPart.split(" *, *");
        for (int i = 0; i < stringParameters.length; i++) {
            stringParameters[i] = removePackage(stringParameters[i]);
        }
        return Arrays.asList(stringParameters);
    }

    private String removePackage(String type) {
        int dot = type.lastIndexOf('.');
        if (dot != -1) {
            return type.substring(dot + 1, type.length());
        }
        return type;
    }
}

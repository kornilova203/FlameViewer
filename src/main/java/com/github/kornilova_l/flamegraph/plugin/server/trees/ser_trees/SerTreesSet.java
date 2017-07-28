package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.outgoing_calls.OutgoingCallsBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.jvmTypeToParam;
import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.splitDesc;

public class SerTreesSet extends TreesSet {
    public SerTreesSet(File logFile) {
        super(logFile);
        callTree = new CallTreesBuilder(logFile).getTrees();
    }

    @Override
    protected void validateExtension() {
        if (ProfilerHttpRequestHandler.getExtension(logFile.getName()) != TreeManager.Extension.SER) {
            throw new IllegalArgumentException("Type is not .ser");
        }
    }

    @Nullable
    @Override
    public TreeProtos.Tree getTree(TreeManager.TreeType treeType) {
        if (callTree == null) {
            return null;
        }
        switch (treeType) {
            case OUTGOING_CALLS:
                if (outgoingCalls == null) {
                    outgoingCalls = new OutgoingCallsBuilder(callTree).getTree();
                }
                return outgoingCalls;
            case INCOMING_CALLS:
                if (incomingCalls == null) {
                    if (outgoingCalls == null) {
                        outgoingCalls = new OutgoingCallsBuilder(callTree).getTree();
                    }
                    if (outgoingCalls == null) {
                        return null;
                    }
                    incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
                }
                return incomingCalls;
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    @Nullable
    @Override
    public TreesProtos.Trees getCallTree() {
        return callTree;
    }

    @NotNull
    @Override
    protected String getBeautifulRetVal(String description) {
        return jvmTypeToParam(description.substring(description.indexOf(")") + 1, description.length()));
    }

    @NotNull
    @Override
    protected List<String> getBeautifulParams(String desc) {
        List<String> jvmParams = splitDesc(desc.substring(1, desc.indexOf(")")));
        return jvmParams.stream()
                .map(MethodConfig::jvmTypeToParam)
                .collect(Collectors.toList());
    }


}

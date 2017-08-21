package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Filter {
    @Nullable
    private final Pattern include;
    @Nullable
    private final Pattern exclude;

    public Filter(@Nullable String include, @Nullable String exclude) {
        this.include = compilePattern(include);
        this.exclude = compilePattern(exclude);
    }

    @Nullable
    private Pattern compilePattern(@Nullable String patternString) {
        if (patternString != null) {
            return Pattern.compile(
                    patternString.replaceAll("\\*", ".*")
                            .replaceAll("\\.", "\\.")
            );
        }
        return null;
    }

    public boolean isNodeIncluded(Node node) {
        String nodeString = getNodeString(node);
        boolean isIncluded = true;
        if (include != null) {
            isIncluded = include.matcher(nodeString).matches();
        }
        boolean isExcluded = false;
        if (exclude != null) {
            isExcluded = exclude.matcher(nodeString).matches();
        }
        return isIncluded &&
                !isExcluded;
    }

    private String getNodeString(Node node) {
        Node.NodeInfo nodeInfo = node.getNodeInfo();
        return nodeInfo.getClassName() + nodeInfo.getMethodName();
    }
}

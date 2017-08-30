package com.github.kornilova_l.flamegraph.plugin.server.trees.util;

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node.NodeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.getSelfTime;

public class MethodTimeCounter {
    private final Tree outgoingCalls;
    private final String className;
    private final String methodName;
    private final String desc;
    private float time = 0;

    public MethodTimeCounter(@NotNull Tree outgoingCalls,
                             @NotNull String className,
                             @NotNull String methodName,
                             @NotNull String desc) {
        this.outgoingCalls = outgoingCalls;
        this.className = className;
        this.methodName = methodName;
        this.desc = desc;
    }

    public float getTime() {
        for (Node node : outgoingCalls.getBaseNode().getNodesList()) { // avoid baseNode
            countTimeRecursively(node);
        }
        return time;
    }

    private void countTimeRecursively(Node node) {
        if (isNeededNode(node)) {
            time += ((float) getSelfTime(node) / outgoingCalls.getWidth());
        }
        for (Node child : node.getNodesList()) {
            countTimeRecursively(child);
        }
    }

    private boolean isNeededNode(Node node) {
        NodeInfo nodeInfo = node.getNodeInfo();
        return Objects.equals(nodeInfo.getClassName(), className) &&
                Objects.equals(nodeInfo.getMethodName(), methodName) &&
                Objects.equals(nodeInfo.getDescription(), desc);
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public abstract class TreesSet {
    protected final File logFile;
    private final HashMap<String, TreeProtos.Tree> methodOutgoingCalls = new HashMap<>();
    private final HashMap<String, TreeProtos.Tree> methodIncomingCalls = new HashMap<>();
    private final List<HotSpot> hotSpots = new ArrayList<>();
    @Nullable
    protected TreesProtos.Trees callTree;
    @Nullable
    protected TreeProtos.Tree outgoingCalls;
    @Nullable
    protected TreeProtos.Tree incomingCalls;

    public TreesSet(File logFile) {
        this.logFile = logFile;
        validateExtension();
    }

    @Nullable
    private static TreeProtos.Tree getTreeForMethod(TreeProtos.Tree sourceTree,
                                                    HashMap<String, TreeProtos.Tree> map,
                                                    String className,
                                                    String methodName,
                                                    String desc,
                                                    boolean isStatic) {
        if (sourceTree == null) {
            return null;
        }
        return map.computeIfAbsent(
                className + methodName + desc,
                n -> new MethodAccumulativeTreeBuilder(
                        sourceTree,
                        className,
                        methodName,
                        desc,
                        isStatic
                ).getTree()
        );
    }

    protected abstract void validateExtension();

    public abstract TreeProtos.Tree getTree(TreeManager.TreeType treeType);

    public final TreeProtos.Tree getTree(TreeManager.TreeType treeType, String className, String methodName, String desc, boolean isStatic) {
        switch (treeType) {
            case OUTGOING_CALLS:
                getTree(TreeManager.TreeType.OUTGOING_CALLS);
                return getTreeForMethod(outgoingCalls, methodOutgoingCalls, className, methodName, desc, isStatic);
            case INCOMING_CALLS:
                getTree(TreeManager.TreeType.INCOMING_CALLS);
                return getTreeForMethod(incomingCalls, methodIncomingCalls, className, methodName, desc, isStatic);
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    public abstract TreesProtos.Trees getCallTree();

    @NotNull List<HotSpot> getHotSpots() {
        if (hotSpots.size() == 0) {
            if (outgoingCalls == null) {
                outgoingCalls = getTree(TreeManager.TreeType.OUTGOING_CALLS);
            }
            if (outgoingCalls == null) {
                return new LinkedList<>();
            }
            TreeMap<HotSpot, HotSpot> hotSpotTreeMap = new TreeMap<>();
            for (TreeProtos.Tree.Node node : outgoingCalls.getBaseNode().getNodesList()) { // avoid baseNode
                getHotSpotsRecursively(node, hotSpotTreeMap);
            }
            hotSpots.addAll(hotSpotTreeMap.values());
            hotSpots.sort((hotSpot1, hotSpot2) -> Float.compare(hotSpot2.relativeTime, hotSpot1.relativeTime));
        }
        return hotSpots;
    }

    private void getHotSpotsRecursively(TreeProtos.Tree.Node node, TreeMap<HotSpot, HotSpot> hotSpotTreeMap) {
        HotSpot hotSpot = new HotSpot(
                node.getNodeInfo().getClassName(),
                node.getNodeInfo().getMethodName(),
                node.getNodeInfo().getDescription()
        );
        hotSpot = hotSpotTreeMap.computeIfAbsent(hotSpot, k -> k);
        assert outgoingCalls != null;
        hotSpot.addTime((float) getSelfTime(node) / outgoingCalls.getWidth());
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            getHotSpotsRecursively(child, hotSpotTreeMap);
        }
    }

    private long getSelfTime(TreeProtos.Tree.Node node) {
        long childTime = 0;
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            childTime += child.getWidth();
        }
        return node.getWidth() - childTime;
    }

    public static class HotSpot implements Comparable<HotSpot> {
        private final String methodName;
        private final String className;
        private final String[] parameters;
        private final String retVal;
        private float relativeTime = 0;

        HotSpot(String className, String methodName, String description) {
            this.className = className;
            this.methodName = methodName;
            String params = description.substring(1, description.indexOf(")"));
            parameters = params.split(", ");
            retVal = description.substring(description.indexOf(")") + 1, description.length());
        }

        @Override
        public int compareTo(@NotNull TreesSet.HotSpot hotSpot) {
            return (className + methodName + String.join("", parameters)).compareTo(
                    hotSpot.className + hotSpot.methodName + String.join("", hotSpot.parameters));
        }

        void addTime(float callRelativeTime) {
            relativeTime += callRelativeTime;
        }
    }
}

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
            if (incomingCalls == null) {
                incomingCalls = getTree(TreeManager.TreeType.INCOMING_CALLS);
            }
            if (incomingCalls == null) {
                return new LinkedList<>();
            }
            TreeMap<HotSpot, HotSpot> hotSpotTreeMap = new TreeMap<>();
            for (TreeProtos.Tree.Node node : incomingCalls.getBaseNode().getNodesList()) { // avoid baseNode
                getHotSpotsRecursively(node, hotSpotTreeMap);
            }
            hotSpots.addAll(hotSpotTreeMap.values());
            hotSpots.sort((hotSpot1, hotSpot2) -> Long.compare(hotSpot2.time, hotSpot1.time));
        }
        return hotSpots;
    }

    private void getHotSpotsRecursively(TreeProtos.Tree.Node node, TreeMap<HotSpot, HotSpot> hotSpotTreeMap) {
        HotSpot hotSpot = new HotSpot(
                node.getNodeInfo().getClassName(),
                node.getNodeInfo().getMethodName(),
                getBeautifulParams(node.getNodeInfo().getDescription()),
                getBeautifulRetVal(node.getNodeInfo().getDescription())
        );
        hotSpot = hotSpotTreeMap.computeIfAbsent(hotSpot, k -> k);
        hotSpot.addTime(node.getWidth());
        for (TreeProtos.Tree.Node child : node.getNodesList()) {
            getHotSpotsRecursively(child, hotSpotTreeMap);
        }
    }

    protected abstract String getBeautifulRetVal(String description);

    abstract protected List<String> getBeautifulParams(String desc);

    public static class HotSpot implements Comparable<HotSpot> {
        private final String methodName;
        private final String className;
        private final List<String> parameters;
        private final String retVal;
        private long time = 0;

        HotSpot(String className, String methodName, List<String> parameters, String retVal) {
            this.className = className;
            this.methodName = methodName;
            this.parameters = parameters;
            this.retVal = retVal;
        }

        @Override
        public int compareTo(@NotNull TreesSet.HotSpot hotSpot) {
            return (className + methodName + String.join("", parameters)).compareTo(
                    hotSpot.className + hotSpot.methodName + String.join("", hotSpot.parameters));
        }

        void addTime(long width) {
            time += width;
        }
    }
}

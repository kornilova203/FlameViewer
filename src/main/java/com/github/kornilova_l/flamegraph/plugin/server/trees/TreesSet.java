package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.MethodAccumulativeTreeBuilder;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.incoming_calls.IncomingCallsBuilder;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node;
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.setNodesOffsetRecursively;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.updateNodeList;

public abstract class TreesSet {
    protected final File logFile;
    private final List<HotSpot> hotSpots = new ArrayList<>();
    @Nullable
    protected TreesProtos.Trees callTree;
    @Nullable
    protected Tree outgoingCalls;
    @Nullable
    private Tree incomingCalls;

    public TreesSet(File logFile) {
        this.logFile = logFile;
        validateExtension();
    }

    @Nullable
    private static Tree getTreeForMethod(Tree sourceTree,
                                         String className,
                                         String methodName,
                                         String desc,
                                         boolean isStatic) {
        if (sourceTree == null) {
            return null;
        }
        return new MethodAccumulativeTreeBuilder(
                sourceTree, className, methodName, desc, isStatic
        ).getTree();
    }

    public static int getMaxDepthRecursively(Node.Builder nodeBuilder, int currentDepth) {
        int maxDepth = currentDepth;
        for (Node.Builder child : nodeBuilder.getNodesBuilderList()) {
            int newDepth = getMaxDepthRecursively(child, currentDepth + 1);
            if (newDepth > maxDepth) {
                maxDepth = newDepth;
            }
        }
        return maxDepth;
    }

    /**
     * This method must be called after offset of nodes is set
     * {@link AccumulativeTreesHelper#setNodesOffsetRecursively}
     *
     * @param treeBuilder set width to this tree
     */
    public static void setTreeWidth(Tree.Builder treeBuilder) {
        Node.Builder baseNode = treeBuilder.getBaseNodeBuilder();
        Node.Builder lastNode = baseNode.getNodesBuilder(baseNode.getNodesCount() - 1);
        treeBuilder.setWidth(
                lastNode.getOffset() + lastNode.getWidth()
        );
    }

    protected abstract void validateExtension();

    public abstract TreesPreview getTreesPreview(@Nullable Configuration configuration);

    public abstract Tree getTree(TreeType treeType,
                                 @Nullable Configuration configuration);

    public final Tree getTree(TreeType treeType,
                              String className,
                              String methodName,
                              String desc,
                              boolean isStatic,
                              @Nullable Configuration configuration) {
        switch (treeType) {
            case OUTGOING_CALLS:
                getTree(TreeType.OUTGOING_CALLS, configuration);
                return getTreeForMethod(outgoingCalls, className, methodName, desc, isStatic);
            case INCOMING_CALLS:
                getTree(TreeType.INCOMING_CALLS, configuration);
                return getTreeForMethod(incomingCalls, className, methodName, desc, isStatic);
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    public abstract TreesProtos.Trees getCallTree(@Nullable Configuration configuration);

    public abstract TreesProtos.Trees getCallTree(@Nullable Configuration configuration, @NotNull List<Integer> threadsIds);

    @NotNull List<HotSpot> getHotSpots() {
        if (hotSpots.size() == 0) {
            if (outgoingCalls == null) {
                outgoingCalls = getTree(TreeType.OUTGOING_CALLS, null);
            }
            if (outgoingCalls == null) {
                return new LinkedList<>();
            }
            TreeMap<HotSpot, HotSpot> hotSpotTreeMap = new TreeMap<>();
            for (Node node : outgoingCalls.getBaseNode().getNodesList()) { // avoid baseNode
                getHotSpotsRecursively(node, hotSpotTreeMap);
            }
            hotSpots.addAll(hotSpotTreeMap.values());
            hotSpots.sort((hotSpot1, hotSpot2) -> Float.compare(hotSpot2.relativeTime, hotSpot1.relativeTime));
        }
        return hotSpots;
    }

    private void getHotSpotsRecursively(Node node, TreeMap<HotSpot, HotSpot> hotSpotTreeMap) {
        HotSpot hotSpot = new HotSpot(
                node.getNodeInfo().getClassName(),
                node.getNodeInfo().getMethodName(),
                node.getNodeInfo().getDescription()
        );
        hotSpot = hotSpotTreeMap.computeIfAbsent(hotSpot, k -> k);
        assert outgoingCalls != null;
        hotSpot.addTime((float) getSelfTime(node) / outgoingCalls.getWidth());
        for (Node child : node.getNodesList()) {
            getHotSpotsRecursively(child, hotSpotTreeMap);
        }
    }

    private long getSelfTime(Node node) {
        long childTime = 0;
        for (Node child : node.getNodesList()) {
            childTime += child.getWidth();
        }
        return node.getWidth() - childTime;
    }

    @NotNull
    protected Tree filterTree(Tree tree,
                              @NotNull Configuration configuration,
                              boolean isCallTree) {
        Tree.Builder filteredTree = Tree.newBuilder();
        filteredTree.setBaseNode(Node.newBuilder());
        if (isCallTree) {
            filteredTree.setTreeInfo(tree.getTreeInfo());
        }
        buildFilteredTreeRecursively(filteredTree.getBaseNodeBuilder(),
                tree.getBaseNode(),
                configuration,
                isCallTree);
        int maxDepth = getMaxDepthRecursively(filteredTree.getBaseNodeBuilder(), 0);
        if (!isCallTree) {
            setNodesOffsetRecursively(filteredTree.getBaseNodeBuilder(), 0);
        } else {
            updateOffset(filteredTree);
        }
        setTreeWidth(filteredTree);
        filteredTree.setDepth(maxDepth);
        return filteredTree.build();
    }

    /**
     * Subtract offset of first node from all offsets
     *
     * @param filteredTree tree
     */
    private void updateOffset(Tree.Builder filteredTree) {
        if (filteredTree.getBaseNodeBuilder().getNodesBuilderList().size() == 0) {
            return;
        }
        long offset = filteredTree.getBaseNodeBuilder().getNodes(0).getOffset();
        if (offset == 0) {
            return;
        }
        for (Node.Builder node : filteredTree.getBaseNodeBuilder().getNodesBuilderList()) {
            updateOffsetRecursively(node, offset);
        }
    }

    private void updateOffsetRecursively(Node.Builder node, long offset) {
        node.setOffset(node.getOffset() - offset);
        for (Node.Builder child : node.getNodesBuilderList()) {
            updateOffsetRecursively(child, offset);
        }
    }

    /**
     * @param nodeBuilder   to this node children will be added
     * @param node          children of this node will be added to nodeBuilder
     * @param configuration decides if child will be added
     * @param isCallTree    if it is a call tree
     */
    private void buildFilteredTreeRecursively(Node.Builder nodeBuilder,
                                              Node node,
                                              @NotNull Configuration configuration,
                                              boolean isCallTree) {

        for (Node child : node.getNodesList()) {
            if (configuration.isMethodInstrumented(PluginConfigManager.newMethodConfig(child))) {
                Node.Builder newNode;
                if (isCallTree) {
                    newNode = copyNode(child);
                    newNode.setOffset(child.getOffset());
                    nodeBuilder.addNodes(newNode);
                    newNode = nodeBuilder.getNodesBuilderList().get(nodeBuilder.getNodesBuilderList().size() - 1);
                } else {
                    newNode = updateNodeList(nodeBuilder, child, -1);
                }
                buildFilteredTreeRecursively(
                        newNode,
                        child,
                        configuration,
                        isCallTree);
            } else {
                buildFilteredTreeRecursively(nodeBuilder, child, configuration, isCallTree);
            }
        }
    }

    @NotNull
    private Node.Builder copyNode(Node node) {
        Node.Builder nodeBuilder = Node.newBuilder();
        nodeBuilder.setWidth(node.getWidth());
        nodeBuilder.setNodeInfo(node.getNodeInfo());
        return nodeBuilder;
    }

    protected Tree getTreeMaybeFilter(TreeType treeType, @Nullable Configuration configuration) {
        switch (treeType) {
            case OUTGOING_CALLS:
                if (configuration == null) {
                    return outgoingCalls;
                }
                return filterTree(outgoingCalls, configuration, false);
            case INCOMING_CALLS:
                if (outgoingCalls == null) {
                    return null;
                }
                if (incomingCalls == null) {
                    incomingCalls = new IncomingCallsBuilder(outgoingCalls).getTree();
                }
                if (configuration == null) {
                    return incomingCalls;
                }
                return filterTree(incomingCalls, configuration, false);
            default:
                throw new IllegalArgumentException("Tree type is not supported");
        }
    }

    public static class HotSpot implements Comparable<HotSpot> {
        private final String methodName;
        private final String className;
        private final String[] parameters;
        @SuppressWarnings("unused")
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

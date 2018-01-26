package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil;
import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree;
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.DescriptionConverter.getBeautifulDesc;

class CTBuilder {
    private final String threadName;
    private long threadStartTime;
    // lowest nodes which does not have common parent.
    // (if they had then this parent will be the only child)
    private List<Node.Builder> children = new LinkedList<>();

    CTBuilder(long threadStartTime, String threadName) {
        this.threadStartTime = threadStartTime;
        this.threadName = threadName;
    }

    long getThreadStartTime() {
        return threadStartTime;
    }

    void addEvent(EventProtos.Event.MethodEvent methodEvent, String className) {
        if (methodEvent.getStartTime() < threadStartTime) {
            threadStartTime = methodEvent.getStartTime();
        }
        Node.Builder node = formNewNode(methodEvent, className);
        List<Node.Builder> childrenOfNode = getChildren(node);
        if (childrenOfNode.size() != 0) {
            for (Node.Builder child : childrenOfNode) {
                children.remove(child);
                node.addNodes(child);
            }
        }
        children.add(node);
    }

    private List<Node.Builder> getChildren(Node.Builder node) {
        LinkedList<Node.Builder> childrenOfNode = new LinkedList<>();
        int size = children.size();
        for (int i = size - 1; i >= 0; i--) {
            Node.Builder child = children.get(i);
            if (node.getOffset() > child.getOffset()) {
                break;
            }
            childrenOfNode.addFirst(child);
        }
        return childrenOfNode;
    }

    private Node.Builder formNewNode(EventProtos.Event.MethodEvent methodEvent,
                                     String className) {
        Node.Builder nodeBuilder = Node.newBuilder()
                .setWidth(methodEvent.getDuration())
                .setOffset(methodEvent.getStartTime())
                .setNodeInfo(
                        Node.NodeInfo.newBuilder()
                                .setMethodName(methodEvent.getMethodName())
                                .setClassName(className)
                                .setDescription(
                                        getBeautifulDesc(methodEvent.getDesc())
                                )
                                .setIsStatic(methodEvent.getIsStatic())
                                .addAllParameters(methodEvent.getParametersList())
                );
        switch (methodEvent.getEndCase()) {
            case THROWABLE:
                nodeBuilder.getNodeInfoBuilder()
                        .setException(methodEvent.getThrowable());
                break;
            case RETURN_VALUE:
                nodeBuilder.getNodeInfoBuilder()
                        .setReturnValue(methodEvent.getReturnValue());
                break;
        }
        return nodeBuilder;

    }

    /**
     * @return built Tree of null if tree is empty
     */
    @Nullable
    Tree getBuiltTree(long startTimeOfFirstThread) {
        if (children.size() == 0) {
            return null;
        }
        Tree.Builder treeBuilder = initTreeBuilder(startTimeOfFirstThread);
        Node.Builder baseNode = Node.newBuilder();
        for (Node.Builder child : children) {
            baseNode.addNodes(child);
        }
        treeBuilder.setBaseNode(baseNode);
        subtractOffsetRecursively(treeBuilder.getBaseNodeBuilder(), threadStartTime);
        TreesUtil.INSTANCE.setTreeWidth(treeBuilder);
        TreesUtil.INSTANCE.setNodesCount(treeBuilder);
        treeBuilder.getTreeInfoBuilder().setStartTime(
                threadStartTime - startTimeOfFirstThread
        );
        int maxDepth = TreesSet.getMaxDepthRecursively(treeBuilder.getBaseNodeBuilder(), 0);
        treeBuilder.setDepth(maxDepth);
        return treeBuilder.build();
    }

    private void subtractOffsetRecursively(Node.Builder node, long threadStartTime) {
        for (Node.Builder child : node.getNodesBuilderList()) {
            child.setOffset(child.getOffset() - threadStartTime);
            subtractOffsetRecursively(child, threadStartTime);

        }
    }

    @NotNull
    private Tree.Builder initTreeBuilder(long startTimeOfFirstThread) {
        return Tree.newBuilder()
                .setTreeInfo(
                        Tree.TreeInfo.newBuilder()
                                .setStartTime(threadStartTime - startTimeOfFirstThread)
                                .setThreadName(threadName)
                );
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree;

import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

class CTBuilder {
    private LinkedList<TreeProtos.Tree.Node.Builder> callStack;
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    @Nullable private TreeProtos.Tree tree = null;
    private int maxDepth = 0;
    private int currentDepth = 0;

    CTBuilder(long startTime, long threadId) {
        treeBuilder.setTreeInfo(
                TreeProtos.Tree.TreeInfo.newBuilder()
                        .setThreadId(threadId)
                        .setStartTime(startTime)
                        .build()
        );
        initCallStack();
    }

    private void initCallStack() {
        callStack = new LinkedList<>();
        callStack.addFirst(TreeProtos.Tree.Node.newBuilder()); // add base node
    }

    void addEvent(EventProtos.Event event) {
        if (treeBuilder == null) {
            throw new AssertionError("Tree was already built");
        }
        if (event.getInfoCase() == EventProtos.Event.InfoCase.ENTER) {
            pushNewNode(event);
        } else { // exit or exception
            finishCall(event);
        }
    }

    private void finishCall(EventProtos.Event event) {
        currentDepth--;
        TreeProtos.Tree.Node.Builder node = callStack.removeFirst();
        finishNode(node, event);
        addNodeToParent(node);
    }

    private void addNodeToParent(TreeProtos.Tree.Node.Builder node) {
        // TODO: check width earlier
        if (node.getWidth() == 0) { // if this node took <1ms
            return;
        }
        callStack.getFirst().addNodes(node);
    }

    private void finishNode(TreeProtos.Tree.Node.Builder nodeBuilder,
                            EventProtos.Event event) {
        if (event.getInfoCase() == EventProtos.Event.InfoCase.EXIT) {
            nodeBuilder.getNodeInfoBuilder()
                    .setReturnValue(
                            event.getExit().getReturnValue()
                    );
        } else { // exception
            nodeBuilder.getNodeInfoBuilder()
                    .setException(
                            event.getException().getObject()
                    );
        }
        long width = event.getTime() - treeBuilder.getTreeInfo().getStartTime() - nodeBuilder.getOffset();
        nodeBuilder.setWidth(width);
    }


    private void pushNewNode(EventProtos.Event event) {
        if (++currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
        TreeProtos.Tree.Node.Builder node = TreeProtos.Tree.Node.newBuilder();
        setNodeInfo(node, event.getEnter());
        long offset = event.getTime() - treeBuilder.getTreeInfo().getStartTime();
        node.setOffset(offset);
        callStack.addFirst(node);
    }

    private static void setNodeInfo(TreeProtos.Tree.Node.Builder node, EventProtos.Event.Enter enter) {
        node.setNodeInfo(
                TreeProtos.Tree.Node.NodeInfo.newBuilder()
                        .setClassName(enter.getClassName().replace('/', '.'))
                        .setMethodName(enter.getMethodName())
                        .setDescription(enter.getDescription())
                        .setIsStatic(enter.getIsStatic())
                        .addAllParameters(enter.getParametersList())
        );
    }

    private void buildTree(long timeOfLastEvent) {
        if (callStack.size() == 1) { // if call stack has only one base node (everything is okay)
            if (callStack.getFirst().getNodesCount() == 0) { // if tree is empty
                tree = null;
                return;
            }
            finishTreeBuilding();
        } else { // something went wrong
            finishAllCallsInStack(timeOfLastEvent);
        }
        tree = treeBuilder.build();
        treeBuilder = null;
    }

    private void finishTreeBuilding() {
        TreeProtos.Tree.Node.Builder baseNode = callStack.removeFirst();
        treeBuilder.setBaseNode(baseNode);
        TreeProtos.Tree.Node lastFinishedNode = baseNode.getNodes(baseNode.getNodesCount() - 1);
        long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();
        treeBuilder.setWidth(treeWidth)
                .setDepth(maxDepth);
    }

    /*
    for all nodes:
    - pop nodeInfo
    - build
    - pop node
    - set nodeInfo
    - set width
    - build and add it to calls of top of stack
    */
    private void finishAllCallsInStack(long timeOfLastEvent) {
        long treeWidth = timeOfLastEvent - treeBuilder.getTreeInfo().getStartTime();
        while (callStack.size() > 1) {
            TreeProtos.Tree.Node.Builder unfinishedNode = callStack.removeFirst();
            unfinishedNode.setWidth(treeWidth - unfinishedNode.getOffset());
            addNodeToParent(unfinishedNode);
        }
        finishTreeBuilding();
    }

    /**
     * @param timeOfLastEvent time of last event is needed if tree has any unfinished methods
     * @return built Tree of null if tree is empty
     */
    @Nullable
    TreeProtos.Tree getBuiltTree(long timeOfLastEvent) {
        if (tree == null) {
            buildTree(timeOfLastEvent);
        }
        if (tree == null ||
                tree.getBaseNode() == null ||
                tree.getBaseNode().getNodesList() == null ||
                tree.getBaseNode().getNodesCount() == 0) {
            return null;
        }
        return tree;
    }
}

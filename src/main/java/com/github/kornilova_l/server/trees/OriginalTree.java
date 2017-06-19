package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.util.LinkedList;

class OriginalTree {
    private final LinkedList<TreeProtos.Tree.Node.NodeInfo.Builder> nodeInfoStack = new LinkedList<>();
    private final LinkedList<TreeProtos.Tree.Node.Builder> nodeStack = new LinkedList<>();
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    private final TreeProtos.Tree.TreeInfo.Builder treeInfoBuilder = TreeProtos.Tree.TreeInfo.newBuilder();
    private TreeProtos.Tree tree = null;
    private int maxDepth = 0;
    private int currentDepth = 0;

    OriginalTree(long startTime, long threadId) {
        treeInfoBuilder.setThreadId(threadId)
                .setStartTime(startTime);
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

    /*
    pop nodeInfoStack
    add result
    pop nodeStack
    set nodeInfo
    set offset and width
    build this node
    add it to node which is on top of stack
     */
    private void finishCall(EventProtos.Event event) {
        currentDepth--;
        TreeProtos.Tree.Node.NodeInfo nodeInfo = buildNodeInfo(
                nodeInfoStack.removeFirst(),
                event
        );
        TreeProtos.Tree.Node node = buildNode(
                nodeStack.removeFirst(),
                nodeInfo,
                event
        );
        if (!nodeStack.isEmpty()) {
            nodeStack.getFirst().addNodes(node);
        } else {
            treeBuilder.addNodes(node);
        }
    }

    private TreeProtos.Tree.Node buildNode(TreeProtos.Tree.Node.Builder nodeBuilder,
                                           TreeProtos.Tree.Node.NodeInfo nodeInfo,
                                           EventProtos.Event event) {
        long width = event.getTime() - treeBuilder.getTreeInfo().getStartTime() - nodeBuilder.getOffset();
        return nodeBuilder.setNodeInfo(nodeInfo)
                .setWidth(width)
                .build();
    }

    private static TreeProtos.Tree.Node.NodeInfo buildNodeInfo(TreeProtos.Tree.Node.NodeInfo.Builder nodeInfoBuilder,
                                                               EventProtos.Event event) {
        if (event.getInfoCase() == EventProtos.Event.InfoCase.EXIT) {
            nodeInfoBuilder.setReturnValue(
                    event.getExit().getReturnValue()
            );
        } else { // exception
            nodeInfoBuilder.setException(
                    event.getException()
            );
        }
        return nodeInfoBuilder.build();
    }


    private void pushNewNode(EventProtos.Event event) {
        if (++currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
        nodeInfoStack.addFirst(
                TreeProtos.Tree.Node.NodeInfo.newBuilder()
                        .setClassName(event.getEnter().getClassName())
                        .setMethodName(event.getEnter().getMethodName())
                        .setIsStatic(event.getEnter().getIsStatic())
                        .addAllParameters(event.getEnter().getParametersList())
        );
        long offset = event.getTime() - treeBuilder.getTreeInfo().getStartTime();
        nodeStack.addFirst(
                TreeProtos.Tree.Node.newBuilder()
                        .setOffset(offset)
        );
    }

    void buildTree() {
        if (nodeInfoStack.isEmpty()) { // everything is okay
            TreeProtos.Tree.Node lastFinishedNode = treeBuilder.getNodes(treeBuilder.getNodesCount() - 1);
            long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();
            treeBuilder.setTreeInfo(
                    treeInfoBuilder.setDuration(treeWidth)
                            .build()
            );
            treeBuilder.setDepth(maxDepth);
            tree = treeBuilder.build();
            treeBuilder = null;
        } else { // something went wrong
            finishAllCallsInStack();
        }
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
    private void finishAllCallsInStack() {
        TreeProtos.Tree.Node lastFinishedNode = treeBuilder.getNodes(treeBuilder.getNodesCount() - 1);
        long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();

        while (!nodeInfoStack.isEmpty()) {
            TreeProtos.Tree.Node.Builder nodeBuilder = nodeStack.removeFirst();
            TreeProtos.Tree.Node node = nodeBuilder
                    .setNodeInfo(
                            nodeInfoStack.removeFirst()
                    )
                    .setWidth(treeWidth - nodeBuilder.getOffset())
                    .build();
            if (!nodeStack.isEmpty()) {
                nodeStack.getFirst().addNodes(node);
            } else {
                treeBuilder.addNodes(node);
            }
        }
    }

    TreeProtos.Tree getBuiltTree() {
        if (tree == null) {
            throw new AssertionError("Tree was not built");
        }
        return tree;
    }
}

package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.util.LinkedList;

class OriginalTree {
    private final LinkedList<TreeProtos.Tree.Node.NodeInfo.Builder> nodeInfoStack = new LinkedList<>();
    private final LinkedList<TreeProtos.Tree.Node.Builder> nodeStack = new LinkedList<>();
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    private TreeProtos.Tree tree = null;
    private int maxDepth = 0;
    private int currentDepth = 0;

    OriginalTree(long startTime, long threadId) {
        treeBuilder.setTreeInfo(
                TreeProtos.Tree.TreeInfo.newBuilder()
                        .setThreadId(threadId)
                        .setStartTime(startTime)
                        .build()
        );
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
                    event.getException().getObject()
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

    void buildTree(long timeOfLastEvent) {
        if (nodeInfoStack.isEmpty()) { // everything is okay
            TreeProtos.Tree.Node lastFinishedNode = treeBuilder.getNodes(treeBuilder.getNodesCount() - 1);
            long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();
            treeBuilder.setWidth(treeWidth)
                    .setDepth(maxDepth);
//            System.out.println("treeBuilder: " + treeBuilder);
//            System.out.println("built tree: " + tree);
        } else { // something went wrong
            finishAllCallsInStack(timeOfLastEvent);
        }
        tree = treeBuilder.build();
        treeBuilder = null;
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
        treeBuilder.setWidth(treeWidth)
                .setDepth(maxDepth);
    }

    /**
     * This method should only be called from {@link #finishAllCallsInStack(long) finishAllCallsInStack()}
     *
     * @return width of tree
     */
    private long getTreeWidth() {
        TreeProtos.Tree.Node.Builder latestNodeBuilder = nodeStack.getFirst();
        if (latestNodeBuilder.getNodesCount() == 0) {
            return latestNodeBuilder.getOffset();
        } else {
            return latestNodeBuilder.getOffset() + latestNodeBuilder.getWidth();
        }
    }

    /**
     * @param timeOfLastEvent time of last event is needed if tree has any unfinished methods
     * @return built Tree
     */
    TreeProtos.Tree getBuiltTree(long timeOfLastEvent) {
        if (tree == null) {
            buildTree(timeOfLastEvent);
        }
        return tree;
    }
}

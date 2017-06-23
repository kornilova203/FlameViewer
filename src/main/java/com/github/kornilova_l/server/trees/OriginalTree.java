package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.util.LinkedList;

class OriginalTree {
    private final LinkedList<UnfinishedNode> unfinishedNodesStack = new LinkedList<>();
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    private TreeProtos.Tree tree = null;
    private int maxDepth = 0;
    private int currentDepth = 0;

    private final class UnfinishedNode {
        final TreeProtos.Tree.Node.NodeInfo.Builder nodeInfoBuilder = TreeProtos.Tree.Node.NodeInfo.newBuilder();
        final TreeProtos.Tree.Node.Builder nodeBuilder = TreeProtos.Tree.Node.newBuilder();
    }

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
        UnfinishedNode unfinishedNode = unfinishedNodesStack.removeFirst();
        TreeProtos.Tree.Node.NodeInfo nodeInfo = buildNodeInfo(
                unfinishedNode.nodeInfoBuilder,
                event
        );
        TreeProtos.Tree.Node node = buildNode(
                unfinishedNode.nodeBuilder,
                nodeInfo,
                event
        );
        addBuiltNode(node);
    }

    private void addBuiltNode(TreeProtos.Tree.Node node) {
        if (node.getWidth() == 0) { // if this node took <1ms
            return;
        }
        if (!unfinishedNodesStack.isEmpty()) {
            unfinishedNodesStack.getFirst().nodeBuilder.addNodes(node);
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
        UnfinishedNode unfinishedNode = new UnfinishedNode();
        unfinishedNode.nodeInfoBuilder
                .setClassName(event.getEnter().getClassName())
                .setMethodName(event.getEnter().getMethodName())
                .setDescription(event.getEnter().getDescription())
                .setIsStatic(event.getEnter().getIsStatic())
                .addAllParameters(event.getEnter().getParametersList());
        long offset = event.getTime() - treeBuilder.getTreeInfo().getStartTime();
        unfinishedNode.nodeBuilder
                .setOffset(offset);
        unfinishedNodesStack.addFirst(unfinishedNode);
    }

    void buildTree(long timeOfLastEvent) {
        if (unfinishedNodesStack.isEmpty()) { // everything is okay
            TreeProtos.Tree.Node lastFinishedNode = treeBuilder.getNodes(treeBuilder.getNodesCount() - 1);
            long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();
            treeBuilder.setWidth(treeWidth)
                    .setDepth(maxDepth);
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
        while (!unfinishedNodesStack.isEmpty()) {
            UnfinishedNode unfinishedNode = unfinishedNodesStack.removeFirst();
            TreeProtos.Tree.Node.Builder nodeBuilder = unfinishedNode.nodeBuilder;
            TreeProtos.Tree.Node node = nodeBuilder
                    .setNodeInfo(
                            unfinishedNode.nodeInfoBuilder
                    )
                    .setWidth(treeWidth - nodeBuilder.getOffset())
                    .build();
            addBuiltNode(node);
        }
        treeBuilder.setWidth(treeWidth)
                .setDepth(maxDepth);
    }

    /**
     * @param timeOfLastEvent time of last event is needed if tree has any unfinished methods
     * @return built Tree of null if tree is empty
     */
    TreeProtos.Tree getBuiltTree(long timeOfLastEvent) {
        if (tree == null) {
            buildTree(timeOfLastEvent);
        }
        if (tree.getNodesCount() == 0) { // if all methods took <1ms
            return null;
        }
        return tree;
    }
}

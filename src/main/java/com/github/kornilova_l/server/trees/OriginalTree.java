package com.github.kornilova_l.server.trees;

import com.github.kornilova_l.protos.EventProtos;
import com.github.kornilova_l.protos.TreeProtos;

import java.util.LinkedList;

class OriginalTree {
    private final LinkedList<TreeProtos.Tree.Call.Builder> callsStack = new LinkedList<>();
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    private TreeProtos.Tree tree = null;
    private int maxDepth = 0;
    private int currentDepth = 0;

    OriginalTree(long startTime, long threadId) {
        treeBuilder.setThreadId(threadId)
                .setStartTime(startTime);
    }

    void addEvent(EventProtos.Event event) {
        if (treeBuilder == null) {
            throw new AssertionError("Tree was already built");
        }
        if (event.getInfoCase() == EventProtos.Event.InfoCase.ENTER) {
            createNewCall(event);
        } else { // exit or exception
            finishCall(event);
        }
    }

    /*
    pop callsStack
    build this call
    add it to calls of call which is on top of stack
     */
    private void finishCall(EventProtos.Event event) {
        currentDepth--;
        TreeProtos.Tree.Call.Builder callBuilder = callsStack.removeFirst();
        if (event.getInfoCase() == EventProtos.Event.InfoCase.EXIT) {
            callBuilder.setExit(event.getExit());
        } else {
            callBuilder.setException(event.getException());
        }
        callBuilder.setDuration(
                event.getTime() - callBuilder.getStartTime()
        );
        if (!callsStack.isEmpty()) {
            callsStack.getFirst().addCalls(
                    callBuilder.build()
            );
        } else {
            treeBuilder.addCalls(callBuilder.build());
        }
    }


    private void createNewCall(EventProtos.Event event) {
        if (++currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
        callsStack.addFirst(
                TreeProtos.Tree.Call.newBuilder()
                        .setEnter(event.getEnter())
                        .setStartTime(event.getTime())
        );
    }

    void buildTree() {
        if (callsStack.isEmpty()) { // everything is okay
            TreeProtos.Tree.Call lastFinishedCall = treeBuilder.getCalls(treeBuilder.getCallsCount() - 1);
            long exitTimeOfLastFinishedCall = lastFinishedCall.getStartTime() + lastFinishedCall.getDuration();
            treeBuilder.setDuration(
                    exitTimeOfLastFinishedCall - treeBuilder.getStartTime()
            );
            treeBuilder.setDepth(maxDepth);
            tree = treeBuilder.build();
            treeBuilder = null;
        } else { // something went wrong
            finishAllCallsInStack();
        }
    }

    /*
    for all calls:
    - pop call
    - set duration
    - build and add it to calls of top of stack
    */
    private void finishAllCallsInStack() {
        TreeProtos.Tree.Call lastFinishedCall = treeBuilder.getCalls(treeBuilder.getCallsCount() - 1);
        long exitTimeOfLastFinishedCall = lastFinishedCall.getStartTime() + lastFinishedCall.getDuration();

        while (!callsStack.isEmpty()) {
            TreeProtos.Tree.Call.Builder callBuilder = callsStack.removeFirst();
            callBuilder.setDuration(
                    exitTimeOfLastFinishedCall - callBuilder.getStartTime()
            );
            if (!callsStack.isEmpty()) {
                callsStack.getFirst().addCalls(callBuilder.build());
            } else {
                treeBuilder.addCalls(callBuilder.build());
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

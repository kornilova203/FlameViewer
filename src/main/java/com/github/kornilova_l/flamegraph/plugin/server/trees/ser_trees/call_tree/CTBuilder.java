package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet;
import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.splitDesc;

class CTBuilder {
    private LinkedList<TreeProtos.Tree.Node.Builder> callStack;
    private TreeProtos.Tree.Builder treeBuilder = TreeProtos.Tree.newBuilder();
    @Nullable
    private TreeProtos.Tree tree = null;

    CTBuilder(long startTime, String threadName) {
        treeBuilder.setTreeInfo(
                TreeProtos.Tree.TreeInfo.newBuilder()
                        .setStartTime(startTime)
                        .setThreadName(threadName)
                        .build()
        );
        initCallStack();
    }

    private static void setNodeInfo(TreeProtos.Tree.Node.Builder node,
                                    EventProtos.Event.MethodEvent.Enter enter,
                                    String className) {
        node.setNodeInfo(
                TreeProtos.Tree.Node.NodeInfo.newBuilder()
                        .setClassName(className.replace('/', '.'))
                        .setMethodName(enter.getMethodName())
                        .setDescription(enter.getDescription())
                        .setIsStatic(enter.getIsStatic())
                        .addAllParameters(enter.getParametersList())
        );
    }

    /**
     * @param desc file specific description
     * @return (Ljava...lang...String...Z)V -> (String, boolean)void
     */
    @NotNull
    private static String getBeautifulDesc(String desc) {
        List<String> jvmParams = splitDesc(desc.substring(1, desc.indexOf(")")));
        List<String> parameters = jvmParams.stream()
                .map(parameter -> {
                    int lastArr = parameter.lastIndexOf('[');
                    lastArr++;
                    StringBuilder parameterBuilder = new StringBuilder(
                            MethodConfig.jvmTypeToParam(parameter.substring(lastArr, parameter.length()))
                    );
                    for (int i = 0; i < lastArr; i++) {
                        parameterBuilder.append("[]");
                    }
                    parameter = parameterBuilder.toString();
                    return parameter;
                })
                .collect(Collectors.toList());

        String jvmRetVal = desc.substring(desc.indexOf(")") + 1, desc.length());

        return "(" +
                String.join(", ", parameters) +
                ")" +
                MethodConfig.jvmTypeToParam(jvmRetVal);
    }

    long getThreadStartTime() {
        return treeBuilder.getTreeInfoBuilder().getStartTime();
    }

    private void initCallStack() {
        callStack = new LinkedList<>();
        callStack.addFirst(TreeProtos.Tree.Node.newBuilder()); // add base node
    }

    void addEvent(EventProtos.Event.MethodEvent methodEvent, String className) {
        assert methodEvent.getInfoCase() == EventProtos.Event.MethodEvent.InfoCase.ENTER;
        if (treeBuilder == null) {
            throw new AssertionError("Tree was already built");
        }
        pushNewNode(methodEvent, className);
    }

    void addEvent(EventProtos.Event.MethodEvent methodEvent) {
        assert methodEvent.getInfoCase() == EventProtos.Event.MethodEvent.InfoCase.EXCEPTION ||
                methodEvent.getInfoCase() == EventProtos.Event.MethodEvent.InfoCase.EXIT;
        if (treeBuilder == null) {
            throw new AssertionError("Tree was already built");
        }
        finishCall(methodEvent);
    }

    private void finishCall(EventProtos.Event.MethodEvent methodEvent) {
        TreeProtos.Tree.Node.Builder node = callStack.removeFirst();
        finishNode(node, methodEvent);
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
                            EventProtos.Event.MethodEvent methodEvent) {
        if (methodEvent.getInfoCase() == EventProtos.Event.MethodEvent.InfoCase.EXIT) {
            nodeBuilder.getNodeInfoBuilder()
                    .setReturnValue(
                            methodEvent.getExit().getReturnValue()
                    );
        } else { // exception
            nodeBuilder.getNodeInfoBuilder()
                    .setException(
                            methodEvent.getException().getObject()
                    );
        }
        long width = methodEvent.getTime() - treeBuilder.getTreeInfo().getStartTime() - nodeBuilder.getOffset();
        nodeBuilder.setWidth(width);
    }

    private void pushNewNode(EventProtos.Event.MethodEvent methodEvent, String className) {
        TreeProtos.Tree.Node.Builder node = TreeProtos.Tree.Node.newBuilder();
        setNodeInfo(node, methodEvent.getEnter(), className);
        long offset = methodEvent.getTime() - treeBuilder.getTreeInfo().getStartTime();
        node.setOffset(offset);
        callStack.addFirst(node);
    }

    private void buildTree(long timeOfLastEvent) {
        if (callStack.size() == 1) { // if call stack has only one base node (everything is okay)
            if (callStack.getFirst().getNodesCount() == 0) { // if tree is empty
                tree = null;
                return;
            }
            finishTreeBuilding();
        } else { // if something went wrong
            finishAllCallsInStack(timeOfLastEvent);
        }
        if (treeBuilder == null) {
            return;
        }
        setBeautifulDescRecursively(treeBuilder.getBaseNodeBuilder());
        tree = treeBuilder.build();
        treeBuilder = null;
    }

    private void setBeautifulDescRecursively(TreeProtos.Tree.Node.Builder node) {
        for (TreeProtos.Tree.Node.Builder child : node.getNodesBuilderList()) {
            child.getNodeInfoBuilder().setDescription(
                    getBeautifulDesc(child.getNodeInfoBuilder().getDescription())
            );
            setBeautifulDescRecursively(child);
        }
    }

    private void finishTreeBuilding() {
        TreeProtos.Tree.Node.Builder baseNode = callStack.removeFirst();
        treeBuilder.setBaseNode(baseNode);
        if (baseNode.getNodesCount() == 0) {
            treeBuilder = null;
            return;
        }
        TreeProtos.Tree.Node lastFinishedNode = baseNode.getNodes(baseNode.getNodesCount() - 1);
        long treeWidth = lastFinishedNode.getOffset() + lastFinishedNode.getWidth();
        int maxDepth = TreesSet.getMaxDepthRecursively(treeBuilder.getBaseNodeBuilder(), 0);
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
        if (treeWidth == 0) {
            treeBuilder = null;
            return;
        }
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
                tree.getBaseNode().getNodesCount() == 0) {
            return null;
        }
        return tree;
    }

    void subtractFromThreadStartTime(long startTimeOfFirstThread) {
        if (treeBuilder == null) {
            throw new RuntimeException("Tree was already build");
        }
        treeBuilder.getTreeInfoBuilder().setStartTime(
                treeBuilder.getTreeInfoBuilder().getStartTime() - startTimeOfFirstThread
        );
    }
}

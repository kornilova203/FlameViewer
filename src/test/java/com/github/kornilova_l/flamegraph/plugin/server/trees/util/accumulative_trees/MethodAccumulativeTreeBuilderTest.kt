package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.incoming_calls.IncomingCallsBuilder
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import org.junit.Assert.assertEquals
import org.junit.Test

class MethodAccumulativeTreeBuilderTest {
    private val className = "MyClass"
    private val methodName = "method"
    private val description = "()void"

    /**
     *  _______
     * |MyClass|___
     * |MyClass____|_________
     * |DifferentClass_______|
     *
     * This test checks that time of MyClass is added only ones
     */
    @Test
    fun testTimePercentInCallTraces() {
        val tree = getTree()
        val methodTree = MethodAccumulativeTreeBuilder(tree, tree, className, methodName,
                description, false).tree

        assertEquals(0.4f, methodTree.treeInfo.timePercent)
    }

    /**
     *  _______________
     * |MyClass________|
     * |MyClass|Diff...|
     * |Diff...|
     */
    @Test
    fun testTimePercentInBackTraces() {
        val tree = getTree()
        val backTraces = IncomingCallsBuilder(tree).tree
        val methodTree = MethodAccumulativeTreeBuilder(backTraces, tree, className, methodName, description, false).tree
        assertEquals(0.4f, methodTree.treeInfo.timePercent)
    }

    private fun getTree(): Tree {
        val treeBuilder = Tree.newBuilder()
        treeBuilder.setBaseNode(Tree.Node.newBuilder())

        val nodeInfo = Tree.Node.NodeInfo.newBuilder()
                .setClassName(className)
                .setMethodName(methodName)
                .setDescription(description)

        val node = Tree.Node.newBuilder()
                .setNodeInfo(Tree.Node.NodeInfo.newBuilder().setClassName("DifferentClass"))
                .setWidth(200)
                .addNodes(Tree.Node.newBuilder()
                        .setNodeInfo(nodeInfo)
                        .setWidth(80)
                        .addNodes(
                                Tree.Node.newBuilder()
                                        .setWidth(50)
                                        .setNodeInfo(nodeInfo)
                        )
                )
        treeBuilder.baseNodeBuilder.addNodes(node)
        setTreeWidth(treeBuilder)
        return treeBuilder.build()
    }
}
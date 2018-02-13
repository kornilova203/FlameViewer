package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees

import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import org.junit.Assert.assertEquals
import org.junit.Test

class CallTracesMethodBuilderTest {

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
        val methodTree = CallTracesMethodBuilder(tree, className, methodName,
                description).tree

        assertEquals(0.4f, methodTree.treeInfo.timePercent)
    }

    companion object {
        const val className = "MyClass"
        const val methodName = "method"
        const val description = "()void"

        fun getTree(): Tree {
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
            TreesUtil.setTreeWidth(treeBuilder)
            TreesUtil.setNodesCount(treeBuilder)
            return treeBuilder.build()
        }

    }
}
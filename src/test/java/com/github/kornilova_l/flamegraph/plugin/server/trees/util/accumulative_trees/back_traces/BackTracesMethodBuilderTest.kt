package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.back_traces

import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.className
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.description
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.getTree
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.methodName
import org.junit.Test
import java.io.File


class BackTracesMethodBuilderTest {
    @Test
    fun simpleTest() {
        val tree = getTree()
        val actualMethodTree = BackTracesMethodBuilder(tree, className, methodName, description).tree
        TestHelper.compare(actualMethodTree.toString(),
                File("src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/util/accumulative_tree/back_traces/simple_backtraces_method_tree.txt"))
    }
}
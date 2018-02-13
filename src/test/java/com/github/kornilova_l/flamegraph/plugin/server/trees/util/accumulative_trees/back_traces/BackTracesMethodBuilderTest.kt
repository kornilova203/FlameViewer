package com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.back_traces

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest
import com.github.kornilova_l.flamegraph.plugin.server.trees.GetTreesTest
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.className
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.description
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.getTree
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.CallTracesMethodBuilderTest.Companion.methodName
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import java.io.ByteArrayInputStream
import java.io.File


class BackTracesMethodBuilderTest : LightPlatformCodeInsightFixtureTestCase() {
    private val dirPath = "src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/util/accumulative_tree/back_traces"

    fun testSimpleTree() {
        val tree = getTree()
        val actualMethodTree = BackTracesMethodBuilder(tree, className, methodName, description).tree
        assertEquals(File("$dirPath/simple_backtraces_method_tree.txt").readText(), actualMethodTree.toString())
    }

    fun testBiggerTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val file = File("$dirPath/biggerTree.flamegraph")
        FilesUploaderTest.sendFile(file.name, file.readBytes())

        val bytes = GetTreesTest.sendRequestForMethodBackTraces(file.name, "", "c", "")
        assertNotNull(bytes)
        val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))
        assertEquals(File("$dirPath/biggerTree.txt").readText(), tree.toString())
    }

    fun testMultipleOccurrenceInStack() {
        PluginFileManager.deleteAllUploadedFiles()
        val file = File("$dirPath/multiple_occurrence_in_stack.flamegraph")
        FilesUploaderTest.sendFile(file.name, file.readBytes())

        val bytes = GetTreesTest.sendRequestForMethodBackTraces(file.name, "", "a", "")
        assertNotNull(bytes)
        val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))
        assertEquals(File("$dirPath/multiple_occurrence_in_stack.txt").readText(), tree.toString())
    }
}
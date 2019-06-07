package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTRACES
import com.github.korniloval.flameviewer.converters.TreeGenerator
import com.github.korniloval.flameviewer.converters.trees.maximumNodesCount
import java.io.ByteArrayInputStream
import java.io.File

class FlamegraphToCallTracesConverterTest : ConverterTestCase("flamegraph", CALLTRACES) {
    private val seed = 0L

    fun testSimple() = doTest()
    fun testOneStacktrace() = doTest()
    fun testBiggerTree() = doTest()
    fun testMultipleOccurrenceInStack() = doTest()
    fun testAsyncProfiler() = doTest()

    fun testNodesCountBiggerThanLimit() {
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("${getProfilerFilesPath()}/nodesCountBiggerThanLimit.flamegraph")
        try {
            val nodesCount = maximumNodesCount + 10_000
            TreeGenerator(nodesCount, seed).outputFlamegraph(tempFile)
            val bytes = getTreeBytes()
            val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))

            assertTrue(tree.visibleDepth != 0)
            assertTrue(tree.depth > tree.visibleDepth)
            assertEquals(nodesCount, tree.treeInfo.nodesCount)

        } finally {
            tempFile.delete()
        }
    }

    fun testZoomedPartOfBigTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("${getProfilerFilesPath()}/zoomedPartOfBigTree.flamegraph")
        try {
            val nodesCount = 50_000 // it is more than maximumNodesCount
            val treeGenerator = TreeGenerator(nodesCount, seed)
            treeGenerator.outputFlamegraph(tempFile)

            val path = ArrayList<Int>()
            val childCount = treeGenerator.root.children.size
            path.add(childCount - 1) // last child in first layer
            val zoomedNode = treeGenerator.root.children[childCount - 1] // get last

            val bytes = getTreeBytes(path = path)
            val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))

            assertEquals(1, tree.baseNode.nodesCount) // only zoomed node
            assertEquals(zoomedNode.name.substring(0, zoomedNode.name.length - 2), tree.baseNode.nodesList[0].nodeInfo.methodName)

        } finally {
            tempFile.delete()
        }
    }
}

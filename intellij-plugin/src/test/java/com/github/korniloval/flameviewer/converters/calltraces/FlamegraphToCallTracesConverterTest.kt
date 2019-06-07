package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTRACES
import com.github.korniloval.flameviewer.converters.TreeGenerator
import com.github.korniloval.flameviewer.server.handlers.countNodes
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.assertNotEquals

class FlamegraphToCallTracesConverterTest : ConverterTestCase("flamegraph", CALLTRACES) {
    private val seed = 0L

    fun testSimple() = doTest()
    fun testOneStacktrace() = doTest()
    fun testBiggerTree() = doTest()
    fun testMultipleOccurrenceInStack() = doTest()
    fun testAsyncProfiler() = doTest()

    fun testNodesCountBiggerThanLimit1() = doTestNodesCountBiggerThanLimit(1)
    fun testNodesCountBiggerThanLimit100() = doTestNodesCountBiggerThanLimit(100)
    fun testNodesCountBiggerThanLimit1000() = doTestNodesCountBiggerThanLimit(1000)

    fun testZoomedPartOfBigTree1() = doTestZoomedPartOfBigTree(1)
    fun testZoomedPartOfBigTree100() = doTestZoomedPartOfBigTree(100)
    fun testZoomedPartOfBigTree1000() = doTestZoomedPartOfBigTree(1000)

    private fun doTestNodesCountBiggerThanLimit(maxNumOfVisibleNodes: Int) {
        val fileName = getTestName(true)
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("${getProfilerFilesPath()}/$fileName.flamegraph")
        try {
            val nodesCount = maxNumOfVisibleNodes * 5
            TreeGenerator(nodesCount, seed).outputFlamegraph(tempFile)
            val bytes = getTreeBytes(fileName = fileName, maxNumOfVisibleNodes = maxNumOfVisibleNodes)
            val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))

            val actualNodesCount = countNodes(tree.baseNode) - 1 // don't count base node
            assertNotEquals(nodesCount, actualNodesCount)
            assertTrue(tree.visibleDepth != 0)
            assertEquals(nodesCount, tree.treeInfo.nodesCount)
        } finally {
            tempFile.delete()
        }
    }

    private fun doTestZoomedPartOfBigTree(maxNumOfVisibleNodes: Int) {
        PluginFileManager.deleteAllUploadedFiles()
        val fileName = getTestName(true)
        val tempFile = File("${getProfilerFilesPath()}/$fileName.flamegraph")
        try {
            val nodesCount = maxNumOfVisibleNodes * 2
            val treeGenerator = TreeGenerator(nodesCount, seed)
            treeGenerator.outputFlamegraph(tempFile)

            val path = ArrayList<Int>()
            val childCount = treeGenerator.root.children.size
            path.add(childCount - 1) // last child in first layer
            val zoomedNode = treeGenerator.root.children[childCount - 1] // get last

            val bytes = getTreeBytes(path = path, fileName = fileName, maxNumOfVisibleNodes = maxNumOfVisibleNodes)
            val tree = TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes))

            assertEquals(1, tree.baseNode.nodesCount) // only zoomed node
            assertEquals(zoomedNode.name.substring(0, zoomedNode.name.length - 2), tree.baseNode.nodesList[0].nodeInfo.methodName)

        } finally {
            tempFile.delete()
        }
    }
}

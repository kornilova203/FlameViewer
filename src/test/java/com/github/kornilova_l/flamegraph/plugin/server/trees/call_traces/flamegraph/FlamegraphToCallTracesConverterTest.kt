package com.github.kornilova_l.flamegraph.plugin.server.trees.call_traces.flamegraph

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeGenerator
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import java.io.ByteArrayInputStream
import java.io.File

class FlamegraphToCallTracesConverterTest : ConverterTestCase("flamegraph", "trees/outgoing-calls") {

    fun testOneStacktrace() = doTest()

    fun testNodesCountBiggerThanLimit() {
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("${getProfilerFilesPath()}/nodesCountBiggerThanLimit.flamegraph")
        try {
            val nodesCount = 20_000 // it is more than maximumNodesCount
            TreeGenerator(nodesCount).outputFlamegraph(tempFile)
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
            val treeGenerator = TreeGenerator(nodesCount)
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

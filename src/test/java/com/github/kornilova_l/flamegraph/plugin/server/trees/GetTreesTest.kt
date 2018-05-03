package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.Assert
import org.jetbrains.ide.BuiltInServerManager
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class GetTreesTest : LightPlatformCodeInsightFixtureTestCase() {
    private val pathToDir = "src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/ser"

    /**
     * Upload ser file that contains class without package
     * send request for method tree
     */
    fun testSerFileMethodTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val serFile = File("$pathToDir/ClassWithoutPackage-2018-02-01-13_55_17.ser")
        FilesUploaderTest.sendFile(serFile.name, serFile.readBytes())

        /* test full tree */
        var bytes = sendRequestForCallTraces(serFile.name)
        assertNotNull(bytes)
        TestHelper.compare(Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("$pathToDir/expected/ClassWithoutPackage.txt"))

        /* test method tree */
        bytes = sendRequestForMethodCallTraces(serFile.name, "ClassWithoutPackage", "fun1", "()void")
        assertNotNull(bytes)
        Assert.assertEquals(File("$pathToDir/expected/ClassWithoutPackage-method.txt").readText(),
                Tree.parseFrom(ByteArrayInputStream(bytes)).toString())
    }

    /**
     * Upload ser file that contains class without package
     * send request for method tree
     */
    fun testSerFileWithStrangeName() {
        PluginFileManager.deleteAllUploadedFiles()
        val serFile = File("$pathToDir/twoDots.and space.ser")
        FilesUploaderTest.sendFile(serFile.name, serFile.readBytes())

        /* test call tree */
        val bytes = sendRequestForCallTree(serFile.name)
        assertNotNull(bytes)
        Assert.assertEquals(File("$pathToDir/expected/twoDots.and space.txt").readText(),
                Trees.parseFrom(ByteArrayInputStream(bytes)).toString())
    }

    fun testGetPartOfTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("temp01.flamegraph")
        val nodesCount = 20_000 // it is more than maximumNodesCount
        TreeGenerator(nodesCount).outputFlamegraph(tempFile)
        FilesUploaderTest.sendFile(tempFile.name, tempFile.readBytes())

        val bytes = sendRequestForCallTraces(tempFile.name)
        assertNotNull(bytes)
        val tree = Tree.parseFrom(ByteArrayInputStream(bytes))

        assertTrue(tree.visibleDepth != 0)
        assertTrue(tree.depth > tree.visibleDepth)
        assertEquals(nodesCount, tree.treeInfo.nodesCount)

        tempFile.delete()
    }

    fun testGetZoomedPartOfTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val tempFile = File("temp02.flamegraph")
        val nodesCount = 50_000 // it is more than maximumNodesCount
        val treeGenerator = TreeGenerator(nodesCount)
        treeGenerator.outputFlamegraph(tempFile)
        FilesUploaderTest.sendFile(tempFile.name, tempFile.readBytes())

        val path = ArrayList<Int>()
        val childCount = treeGenerator.root.children.size
        path.add(childCount - 1) // last child in first layer
        val zoomedNode = treeGenerator.root.children[childCount - 1] // get last

        val bytes = sendRequestForCallTraces(tempFile.name, path)
        assertNotNull(bytes)

        val tree = Tree.parseFrom(ByteArrayInputStream(bytes))

        assertEquals(1, tree.baseNode.nodesCount) // only zoomed node
        assertEquals(zoomedNode.name.substring(0, zoomedNode.name.length - 2), tree.baseNode.nodesList[0].nodeInfo.methodName)

        tempFile.delete()
    }

    companion object {
        /**
         * @param path to zoomed node (null if node is not zoomed)
         */
        fun sendRequestForCallTraces(fileName: String, path: List<Int>? = null): ByteArray {
            val address = StringBuilder().append("http://localhost:${BuiltInServerManager.getInstance().port}")
                    .append("/flamegraph-profiler/trees/outgoing-calls?")
                    .append("file=").append(fileName)
                    .append("&project=uploaded-files")
            if (path != null) { // if node is zoomed
                for (index in path) {
                    address.append("&path=").append(index)
                }
            }
            val url = URL(address.toString())
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }

        fun sendRequestForCallTree(fileName: String): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/trees/call-tree?" +
                    "file=${fileName.replace(" ", "%20")}" +
                    "&project=uploaded-files")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }

        fun sendRequestForMethodCallTraces(fileName: String, className: String, methodName: String, description: String): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/trees/outgoing-calls?" +
                    "file=$fileName" +
                    "&project=uploaded-files" +
                    "&class=$className" +
                    "&method=$methodName" +
                    "&desc=$description")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }

        fun sendRequestForMethodBackTraces(fileName: String, className: String, methodName: String, description: String): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/trees/incoming-calls?" +
                    "file=$fileName" +
                    "&project=uploaded-files" +
                    "&class=$className" +
                    "&method=$methodName" +
                    "&desc=$description")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }
    }
}
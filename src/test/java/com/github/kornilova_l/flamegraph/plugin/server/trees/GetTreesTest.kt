package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
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
        TestHelper.compare(Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("$pathToDir/expected/ClassWithoutPackage-method.txt"))
    }

    fun testGetPartOfTree() {
        PluginFileManager.deleteAllUploadedFiles()
        val serFile = File("$pathToDir/ClassWithoutPackage-2018-02-01-13_55_17.ser")
        FilesUploaderTest.sendFile(serFile.name, serFile.readBytes())

        val bytes = sendRequestForPartOfCallTree(serFile.name, listOf(0))
        assertNotNull(bytes)
        TestHelper.compare(Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("$pathToDir/expected/ClassWithoutPackage-part.txt"))
    }

    companion object {
        fun sendRequestForCallTraces(fileName: String): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/trees/outgoing-calls?" +
                    "file=$fileName&" +
                    "project=uploaded-files")
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

        fun sendRequestForPartOfCallTree(fileName: String, path: List<Int>): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/trees/outgoing-calls?" +
                    "file=$fileName" +
                    "&project=uploaded-files" +
                    "&path=$path")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }
    }
}
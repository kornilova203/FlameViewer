package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest.Companion.getResponse
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest.Companion.sendFile
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.ide.BuiltInServerManager
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class FileToCallTracesTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testFlamegraph() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val flamegraphFile = File("src/test/resources/StacksOCTreeBuilderTest/test_data01.txt")
        sendFile(flamegraphFile.name, flamegraphFile.readBytes())
        val bytes = sendRequestForCallTraces(flamegraphFile.name)
        assertNotNull(bytes)
        TestHelper.compare(TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("src/test/resources/StacksOCTreeBuilderTest/result01.txt"))
    }

    fun testYourkitCsv() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val yourkitCsvFile = File("src/test/resources/file_to_call_traces_converter/yourkit_csv/simple.csv")
        sendFile(yourkitCsvFile.name, yourkitCsvFile.readBytes())
        val bytes = sendRequestForCallTraces(yourkitCsvFile.name)
        assertNotNull(bytes)
        TestHelper.compare(TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("src/test/resources/file_to_call_traces_converter/yourkit_csv/expected/simple.txt"))
    }

    private fun sendRequestForCallTraces(fileName: String): ByteArray {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                "/flamegraph-profiler/trees/outgoing-calls?" +
                "file=$fileName&" +
                "project=uploaded-files")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return getResponse(connection)
    }
}
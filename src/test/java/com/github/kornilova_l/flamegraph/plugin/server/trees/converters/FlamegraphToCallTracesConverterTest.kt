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

class FlamegraphToCallTracesTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testCallTracesBuilding() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val flamegraphFile = File("src/test/resources/StacksOCTreeBuilderTest/test_data01.txt")
        sendFile(flamegraphFile.name, flamegraphFile.readBytes())
        val bytes = sendRequestForCallTraces(flamegraphFile.name)
        assertNotNull(bytes)
        TestHelper.compare(TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("src/test/resources/StacksOCTreeBuilderTest/result01.txt"))
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
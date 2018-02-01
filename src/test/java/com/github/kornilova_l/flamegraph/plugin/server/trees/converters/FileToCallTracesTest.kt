package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest.Companion.sendFile
import com.github.kornilova_l.flamegraph.plugin.server.trees.GetTreesTest.Companion.sendRequestForCallTraces
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import java.io.ByteArrayInputStream
import java.io.File

class FileToCallTracesTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testFlamegraph() {
        PluginFileManager.deleteAllUploadedFiles()
        val flamegraphFile = File("src/test/resources/StacksOCTreeBuilderTest/test_data01.txt")
        sendFile(flamegraphFile.name, flamegraphFile.readBytes())
        val bytes = sendRequestForCallTraces(flamegraphFile.name)
        assertNotNull(bytes)
        TestHelper.compare(TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("src/test/resources/StacksOCTreeBuilderTest/result01.txt"))
    }

    fun testYourkitCsv() {
        doTest("simple")
        doTest("01_yourkit_test")
        doTest("02_yourkit_test")
    }

    private fun doTest(fileName: String) {
        PluginFileManager.deleteAllUploadedFiles()
        val yourkitCsvFile = File("src/test/resources/file_to_call_traces_converter/yourkit_csv/$fileName.csv")
        sendFile(yourkitCsvFile.name, yourkitCsvFile.readBytes())
        val bytes = sendRequestForCallTraces(yourkitCsvFile.name)
        assertNotNull(bytes)
        TestHelper.compare(TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                File("src/test/resources/file_to_call_traces_converter/yourkit_csv/expected/$fileName.txt"))
    }
}
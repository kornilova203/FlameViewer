package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest
import com.github.kornilova_l.flamegraph.plugin.server.trees.GetTreesTest
import com.github.kornilova_l.flamegraph.plugin.server.trees.TestHelper
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import java.io.ByteArrayInputStream
import java.io.File

private const val simpleFileContent = """
a 100 1
b 40 2
c 5 3
d 5 3
e 5 2
f 5 1
"""

private const val fileWithParameters = """
a(a, b) 100 1
b(c) 40 2
c(hello) 5 3
d() 5 3
e(e, se, ef) 5 2
f() 5 1
"""

private const val mixedContent = """
retVal a(a, b) 100 1
Class.b 40 2
Class.c(hello) 5 3
ret Class.c(hello) 5 3
myRetVal d 5 3
e(e, se, ef) 5 2
f 5 1
"""

class CompressedFlamegraphToCallTracesConverterTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testSimpleFile() {
        doTest("simpleFile.cflamegraph",
                simpleFileContent.toByteArray(),
                File("src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/converters/simple-cflamegraph-result.txt")
        )
    }

    fun testFileWithParameters() {
        doTest("fileWithParameters.cflamegraph",
                fileWithParameters.toByteArray(),
                File("src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/converters/with-parameters-cflamegraph-result.txt")
        )
    }

    fun testFileWithMixedContent() {
        doTest("fileWithMixedContent.cflamegraph",
                mixedContent.toByteArray(),
                File("src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees/converters/mixed-content-cflamegraph-result.txt")
        )
    }

    private fun doTest(fileName: String, content: ByteArray, expectedResult: File) {
        PluginFileManager.deleteAllUploadedFiles()
        FilesUploaderTest.sendFile(fileName, content)
        val bytes = GetTreesTest.sendRequestForCallTraces(fileName)
        assertNotNull(bytes)
        TestHelper.compare(
                TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString(),
                expectedResult)
    }
}
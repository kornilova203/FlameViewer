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
--M-- 6
a 0
b 1
c 2
d 3
e 4
f 5
M0w100d1
M1w40d2
M2w5d3
M3w5d3
M4w5d2
M5w5d1
"""

private const val fileWithParameters = """
--M-- 6
a 0
b 1
c 2
d 3
e 4
f 5
--D-- 5
(a, b) 0
(c) 1
(hello) 2
() 3
(e, se, ef) 4
M0D0w100d1
M1D1w40d2
M2D2w5d3
M3D3w5d3
M4D4w5d2
M5D3w5d1
"""

private const val mixedContent = """
--M-- 6
a 0
b 1
c 2
d 3
e 4
f 5
--C-- 1
Class 0
--D-- 5
(a, b)retVal 0
(hello) 1
(hello)ret 2
()myRetVal 3
(e, se, ef) 4

M0D0w100d1
C0M1w40d2
C0M2D1w5d3
C0M2D2w5d3
M3D3w5d3
M4D4w5d2
M5w5d1
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
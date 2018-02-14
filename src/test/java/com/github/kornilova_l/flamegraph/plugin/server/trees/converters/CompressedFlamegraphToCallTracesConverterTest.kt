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
M=0 w=100 d=1
M=1 w=40 d=2
M=2 w=5 d=3
M=3 w=5 d=3
M=4 w=5 d=2
M=5 w=5 d=1
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
M=0 D=0 w=100 d=1
M=1 D=1 w=40 d=2
M=2 D=2 w=5 d=3
M=3 D=3 w=5 d=3
M=4 D=4 w=5 d=2
M=5 D=3 w=5 d=1
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

M=0 D=0 w=100 d=1
C=0 M=1 w=40 d=2
C=0 M=2 D=1 w=5 d=3
C=0 M=2 D=2 w=5 d=3
M=3 D=3 w=5 d=3
M=4 D=4 w=5 d=2
M=5 f w=5 d=1
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
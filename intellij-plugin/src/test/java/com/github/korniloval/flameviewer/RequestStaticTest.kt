package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.server.OUTGOING_CALLS
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.util.*

class RequestStaticTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testRequestHtml() = doTest(OUTGOING_CALLS, "Back Traces")
    fun testRequestCss() = doTest("css/main.css", "body {")
    fun testRequestJs() = doTest("js/out/accumulative-trees.js", "drawCallTraces")

    private fun doTest(path: String, resultContains: String) {
        val urlBuilder = UploadFileUtil.getUrlBuilderBase()
                .addPathSegments(path)
                .addQueryParameter("file", "some-file.cflamegraph")

        val url = urlBuilder.build().url()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val bytes = UploadFileUtil.getResponse(connection)
        ByteArrayInputStream(bytes).use { stream ->
            val scanner = Scanner(stream)
            val sb = StringBuilder()
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine()).append("\n")
            }
            val res = sb.toString()
            TestCase.assertTrue(res, res.contains(resultContains))
        }
    }
}

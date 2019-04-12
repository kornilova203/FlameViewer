package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.server.CALL_TRACES_NAME
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import junit.framework.TestCase
import java.net.HttpURLConnection

class RequestStaticTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testRequestHtml() = doTest(CALL_TRACES_NAME, "Call Traces")
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
        val res = String(bytes)
        TestCase.assertTrue(res, res.contains(resultContains))
    }
}

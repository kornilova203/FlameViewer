package com.github.korniloval.flameviewer.converters

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.UploadFileUtil
import com.github.korniloval.flameviewer.converters.ResultType.*
import com.github.korniloval.flameviewer.server.DEFAULT_MAX_NUM_OF_VISIBLE_NODES
import com.github.korniloval.flameviewer.server.IntellijRequestHandler
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.ide.HttpRequestHandler
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection

abstract class ConverterTestCase(private val fileExtension: String, private val resultType: ResultType) : LightPlatformCodeInsightFixtureTestCase() {

    private val commonSourceFilesPath = "src/test/resources/profiler-files"

    override fun getTestDataPath(): String {
        return "src/test/resources/${resultType.name.toLowerCase()}/$fileExtension"
    }

    protected fun getProfilerFilesPath(): String {
        return "$commonSourceFilesPath/$fileExtension"
    }

    protected fun getTreeBytes(path: List<Int> = ArrayList(), className: String? = null, methodName: String? = null,
                               description: String? = null, fileName: String? = null, include: String? = null,
                               exclude: String? = null, maxNumOfVisibleNodes: Int? = null): ByteArray {
        PluginFileManager.deleteAllUploadedFiles()

        val name = fileName ?: getTestName(true)

        val fileToUpload = File("${getProfilerFilesPath()}/$name.$fileExtension")

        PluginFileManager.deleteAllUploadedFiles()
        UploadFileUtil.sendFile(fileToUpload.name, fileToUpload.readBytes())
        val bytes = withMaxNumOfVisibleNodes(maxNumOfVisibleNodes) {
            sendRequestForTree(fileToUpload.name, path, className, methodName, description, include, exclude)
        }
        assertNotNull(bytes)

        return bytes
    }

    protected fun doTest(path: List<Int> = ArrayList(), className: String? = null, methodName: String? = null,
                         description: String? = null, fileName: String? = null, include: String? = null,
                         exclude: String? = null, maxNumOfVisibleNodes: Int? = null) {
        val bytes = getTreeBytes(path, className, methodName, description, fileName, include, exclude, maxNumOfVisibleNodes)

        val actual = when (resultType) {
            CALLTREE -> TreesProtos.Trees.parseFrom(ByteArrayInputStream(bytes)).toString()
            PREVIEW -> TreesPreviewProtos.TreesPreview.parseFrom(ByteArrayInputStream(bytes)).toString()
            HOTSPOTS -> String(bytes).replace("},{", "},\n{")
            else -> TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString()
        }

        val name = fileName ?: getTestName(true)
        var expectedCallTracesName = "$testDataPath/$name"
        if (className != null && methodName != null && description != null) {
            expectedCallTracesName += "-method"
        }
        if (path.isNotEmpty()) {
            expectedCallTracesName += "-zoomed"
        }
        if (include != null) {
            expectedCallTracesName += "-include=$include"
        }
        if (exclude != null) {
            expectedCallTracesName += "-exclude=$exclude"
        }

        assertSameLinesWithFile(File("$expectedCallTracesName.txt").absolutePath, actual)
    }

    private fun <T> withMaxNumOfVisibleNodes(maxNumOfVisibleNodes: Int?, action: () -> T): T {
        if (maxNumOfVisibleNodes == null) {
            return action()
        }
        setMaxNumOfVisibleNodes(maxNumOfVisibleNodes)
        try {
            return action()
        } finally {
            setMaxNumOfVisibleNodes(DEFAULT_MAX_NUM_OF_VISIBLE_NODES)
        }
    }

    private fun setMaxNumOfVisibleNodes(maxNumOfVisibleNodes: Int) {
        for (handler in HttpRequestHandler.EP_NAME.extensions()) {
            if (handler !is IntellijRequestHandler) continue
            handler.setMaxNumOfVisibleNodes(maxNumOfVisibleNodes)
            return
        }
        throw AssertionError("${IntellijRequestHandler::class.java.simpleName} not found")
    }

    private fun sendRequestForTree(fileName: String, path: List<Int>, className: String?,
                                   methodName: String?, description: String?,
                                   include: String?, exclude: String?): ByteArray {
        val urlBuilder = UploadFileUtil.getUrlBuilderBase()
                .addPathSegments(resultType.url)
                .addQueryParameter("file", fileName)
        path.forEach { index -> urlBuilder.addQueryParameter("path", index.toString()) }

        if (className != null && methodName != null && description != null) {
            urlBuilder.addQueryParameter("class", className)
                    .addQueryParameter("method", methodName)
                    .addQueryParameter("desc", description)
        }

        if (include != null) {
            urlBuilder.addQueryParameter("include", include)
        }

        if (exclude != null) {
            urlBuilder.addQueryParameter("exclude", exclude)
        }

        val url = urlBuilder.build().url()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return UploadFileUtil.getResponse(connection)

    }

}

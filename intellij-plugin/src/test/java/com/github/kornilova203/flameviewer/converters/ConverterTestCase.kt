package com.github.kornilova203.flameviewer.converters

import com.github.kornilova203.flameviewer.PluginFileManager
import com.github.kornilova203.flameviewer.UploadFileUtil
import com.github.kornilova203.flameviewer.converters.ResultType.*
import com.github.kornilova203.flameviewer.server.DEFAULT_MAX_NUM_OF_VISIBLE_NODES
import com.github.kornilova203.flameviewer.server.IntellijRequestHandler
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.ide.BuiltInServerManager
import org.jetbrains.ide.HttpRequestHandler
import java.io.ByteArrayInputStream
import java.io.File
import java.net.HttpURLConnection

abstract class ConverterTestCase(private val fileExtension: String, private val resultType: ResultType) : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        BuiltInServerManager.getInstance().waitForStart()
        PluginFileManager.deleteAllUploadedFiles()
    }

    private val commonSourceFilesPath = "src/test/resources/profiler-files"

    override fun getTestDataPath(): String {
        return "src/test/resources/${resultType.name.toLowerCase()}/$fileExtension"
    }

    protected fun getProfilerFilesPath(): String {
        return "$commonSourceFilesPath/$fileExtension"
    }

    protected fun getTreeBytes(options: ConvertTestOptions): ByteArray {
        val name = options.fileName ?: getTestName(true)
        val fileToUpload = File("${getProfilerFilesPath()}/$name.$fileExtension")
        UploadFileUtil.sendFile(fileToUpload.name, fileToUpload.readBytes())
        val bytes = withMaxNumOfVisibleNodes(options.maxNumOfVisibleNodes) {
            sendRequestForTree(fileToUpload.name, options.path, options.className, options.methodName,
                    options.description, options.include, options.includeStacktrace)
        }
        assertNotNull(bytes)

        return bytes
    }

    protected fun doTest(options: ConvertTestOptions = opt()) {
        val bytes = getTreeBytes(options)

        val actual = when (resultType) {
            CALLTREE -> TreesProtos.Trees.parseFrom(ByteArrayInputStream(bytes)).toString()
            PREVIEW -> TreesPreviewProtos.TreesPreview.parseFrom(ByteArrayInputStream(bytes)).toString()
            HOTSPOTS -> String(bytes).replace("},{", "},\n{")
            else -> TreeProtos.Tree.parseFrom(ByteArrayInputStream(bytes)).toString()
        }

        val expectedFileName = getFileName(options)
        assertSameLinesWithFile(File("$expectedFileName.txt").absolutePath, actual)
    }

    private fun getFileName(options: ConvertTestOptions): String {
        val name = options.fileName ?: getTestName(true)
        val fileName = StringBuilder("$testDataPath/$name")
        if (options.className != null && options.methodName != null && options.description != null) {
            fileName.append("-method")
        }
        if (options.path.isNotEmpty()) {
            fileName.append("-path=").append(options.path.joinToString(separator = ","))
        }
        if (options.maxNumOfVisibleNodes != null) {
            fileName.append("-visible=").append(options.maxNumOfVisibleNodes)
        }
        if (options.include != null) {
            fileName.append("-include=").append(options.include)
        }
        if (options.includeStacktrace != null) {
            fileName.append("-includeStacktrace=").append(options.includeStacktrace)
        }
        return fileName.toString()
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
                                   methodName: String?, description: String?, include: String?,
                                   includeStacktrace: Boolean?): ByteArray {
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
        if (includeStacktrace == true) {
            urlBuilder.addQueryParameter("include-stacktrace", includeStacktrace.toString())
        }

        val url = urlBuilder.build().toUrl()
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        return UploadFileUtil.getResponse(connection)

    }
}

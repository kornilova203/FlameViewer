package com.github.kornilova_l.flamegraph.plugin.server

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.UploadFileUtil.bytesInMB
import com.github.kornilova_l.flamegraph.plugin.server.UploadFileUtil.sendFile
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import io.netty.handler.codec.http.HttpResponseStatus
import org.jetbrains.ide.BuiltInServerManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class FilesUploaderTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testUploadBigFiles() {
        PluginFileManager.deleteAllUploadedFiles()
        val bytes = createBytes(bytesInMB * 50)
        sendFile("big.ser", bytes)
        var expectedFile = PluginFileManager.tempFileSaver.save(bytes, "01_medium.ser")!!
        fileReceivedTest("big.ser", expectedFile)

        /* send parts in reversed order */
        PluginFileManager.deleteAllUploadedFiles()
        sendFile("big.ser", bytes, true)
        expectedFile = PluginFileManager.tempFileSaver.save(bytes, "01_medium.ser")!!
        fileReceivedTest("big.ser", expectedFile)
    }

    fun testGetNonExistingFiles() {
        PluginFileManager.deleteAllUploadedFiles()
        var responseCode = sendRequestDoesFileExist("file-does-not-exist.txt")
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), responseCode)

        sendFile("not-supported-file.txt", ByteArray(100))
        responseCode = sendRequestDoesFileExist("not-supported-file.txt")
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), responseCode)

        sendFile("supported-file.ser", ByteArray(100))
        responseCode = sendRequestDoesFileExist("supported-file.ser")
        assertEquals(HttpResponseStatus.FOUND.code(), responseCode)
    }

    private fun sendRequestDoesFileExist(fileName: String): Int {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}/flamegraph-profiler/does-file-exist")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.setRequestProperty("File-Name", fileName)
        connection.setRequestProperty("Project-Name", "uploaded-files")

        return connection.responseCode
    }

    /**
     * Creates byte array that contains lines
     * each line contains a number == number of current line
     */
    private fun createBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        var lineNumber = 0
        var currentSize = 0
        while (currentSize < size) {
            val newBytes = "$lineNumber. Some text that is needed to increase size of lines\n".toByteArray()
            val newSize = Math.min(bytes.size, currentSize + newBytes.size)
            System.arraycopy(newBytes, 0, bytes, currentSize, newSize - currentSize)
            currentSize = newSize
            lineNumber++
        }
        return bytes
    }

    private fun fileReceivedTest(fileName: String, expectedFile: File) {
        val file = PluginFileManager.getLogFile("uploaded-files", fileName)
        assertNotNull(file)
        assertEquals(file!!.readLines(), expectedFile.readLines())
    }
}
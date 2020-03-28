package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.UploadFileUtil.bytesInMB
import com.github.korniloval.flameviewer.UploadFileUtil.sendFile
import com.github.korniloval.flameviewer.server.DOES_FILE_EXIST
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.ide.BuiltInServerManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class FilesUploaderTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        BuiltInServerManager.getInstance().waitForStart()
        PluginFileManager.deleteAllUploadedFiles()
    }

    fun testUploadBigFiles() {
        val bytes = createBytes(bytesInMB * 50)
        sendFile("big.ser", bytes)
        var expectedFile = PluginFileManager.tempFileSaver.save(bytes, "big.ser")!!
        fileReceivedTest("big.ser", expectedFile)

        /* send parts in reversed order */
        PluginFileManager.deleteAllUploadedFiles()
        sendFile("big.ser", bytes, true)
        expectedFile = PluginFileManager.tempFileSaver.save(bytes, "big.ser")!!
        fileReceivedTest("big.ser", expectedFile)
    }

    fun testUploadFierixFiles() {
        val bytes = createBytes(bytesInMB * 50)
        sendFile("my.fierix", bytes)
        val expectedFile = PluginFileManager.tempFileSaver.save(bytes, "my.fierix")!!
        fileReceivedTest("my.fierix", expectedFile)
    }

    fun testGetNonExistingFiles() {
        var res = sendRequestDoesFileExist("file-does-not-exist.txt")
        assertEquals("{\"result\": false}", res)

        sendFile("not-supported-file.txt", ByteArray(100))
        res = sendRequestDoesFileExist("not-supported-file.txt")
        assertEquals("{\"result\": false}", res)

        sendFile("supported-file.ser", ByteArray(100))
        res = sendRequestDoesFileExist("supported-file.ser")
        assertEquals("{\"result\": true}", res)
    }

    private fun sendRequestDoesFileExist(fileName: String): String {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}$DOES_FILE_EXIST")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.setRequestProperty("File-Name", fileName)

        return String(UploadFileUtil.getResponse(connection))
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
        val file = PluginFileManager.getLogFile(fileName)
        assertNotNull(file)
        assertEquals(file!!.readLines(), expectedFile.readLines())
    }
}
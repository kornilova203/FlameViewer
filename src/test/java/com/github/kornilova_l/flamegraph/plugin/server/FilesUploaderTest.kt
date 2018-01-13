package com.github.kornilova_l.flamegraph.plugin.server

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.ide.BuiltInServerManager
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class FilesUploaderTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testUploadSmallFiles() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        sendFile("small.ser", createBytes(1000))
        fileReceivedTest("small.ser", 1000)
    }

    fun testUploadMediumFiles() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val bytes = createBytes(bytesInMB * 10)
        sendFile("01_medium.ser", bytes)
        val expectedFile = PluginFileManager.getInstance().tempFileSaver.save(bytes, "01_medium.ser")!!
        fileReceivedTest("01_medium.ser", expectedFile)

        sendFile("02_medium.ser", ByteArray(bytesInMB * 90))
        fileReceivedTest("02_medium.ser", bytesInMB * 90)

        sendFile("03_medium.ser", ByteArray(bytesInMB * 100))
        fileReceivedTest("03_medium.ser", bytesInMB * 100)
    }

    fun testUploadBigFiles() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        sendFile("big.ser", ByteArray(bytesInMB * 150))
        fileReceivedTest("big.ser", bytesInMB * 150)
    }

    fun testJfrFileUpload() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val jmcFive = File("src/test/resources/jfr_files/jmc_5_recording.jfr")
        sendFile(jmcFive.name, jmcFive.readBytes())
        fileReceivedTest(jmcFive.name, File("src/test/resources/jfr_files/expected/jmc_5_recording.flamegraph"))
    }

    fun testFlamegraphFileUpload() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        val flamegraphFile = File("src/test/resources/StacksOCTreeBuilderTest/test_data01.txt")
        sendFile(flamegraphFile.name, flamegraphFile.readBytes())
        fileReceivedTest(flamegraphFile.name, flamegraphFile) // file should stay the same
    }

    fun testIfFileUploaded() {
        PluginFileManager.getInstance().deleteAllUploadedFiles()
        var responseCode = sendRequestDoesFileExist("file-does-not-exist.txt")
        assertEquals(400, responseCode)

        sendFile("not-supported-file.txt", ByteArray(100))
        responseCode = sendRequestDoesFileExist("not-supported-file.txt")
        assertEquals(400, responseCode)

        sendFile("supported-file.ser", ByteArray(100))
        responseCode = sendRequestDoesFileExist("supported-file.ser")
        assertEquals(200, responseCode)
    }

    private fun sendRequestDoesFileExist(fileName: String): Int {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}/flamegraph-profiler/does-file-exist")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.setRequestProperty("File-Name", fileName)

        return connection.responseCode
    }

    /**
     * Creates byte array that contains lines
     * each line contains a number == number of current line
     */
    private fun createBytes(size: Int): ByteArray {
        val bytesList = ArrayList<Byte>()
        var lineNumber = 0
        while (bytesList.size < size) {
            bytesList.addAll("$lineNumber\n".toByteArray().toList())
            lineNumber++
        }
        return ByteArray(size) { i -> bytesList[i] }
    }

    private fun fileReceivedTest(fileName: String, fileSize: Int) {
        val file = PluginFileManager.getInstance().getLogFile("uploaded-files", fileName)
        assertNotNull(file)
        assertEquals(fileSize, file!!.readBytes().size)
    }

    private fun fileReceivedTest(fileName: String, expectedFile: File) {
        val file = PluginFileManager.getInstance().getLogFile("uploaded-files", fileName)
        assertNotNull(file)
        assertEquals(file!!.readLines(), expectedFile.readLines())
    }

    companion object {
        private val bytesInMB = 1_000_000
        private val megabytesInOnePart = 100

        fun sendFile(fileName: String, bytes: ByteArray) {
            var partsCount = bytes.size / (bytesInMB * megabytesInOnePart)
            if (bytes.size % (bytesInMB * megabytesInOnePart) != 0) {
                partsCount++
            }
            /* do not pause in following block while debugging. It may break connection */
            for (i in 0 until partsCount) {
                println("request")
                //Create connection
                val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}/flamegraph-profiler/upload-file")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.useCaches = false
                connection.doOutput = true

                val contentLength = if (i == partsCount - 1)
                    bytes.size % (bytesInMB * megabytesInOnePart)
                else
                    bytesInMB * megabytesInOnePart

                connection.setRequestProperty("Content-Length", Integer.toString(contentLength))
                connection.setRequestProperty("File-Part", "${i + 1}/$partsCount")
                connection.setRequestProperty("File-Name", fileName)

                //Send request
                val wr = DataOutputStream(
                        connection.outputStream)
                wr.write(
                        Arrays.copyOfRange(
                                bytes,
                                i * bytesInMB * megabytesInOnePart,
                                Math.min(bytes.size, (i + 1) * bytesInMB * megabytesInOnePart)
                        )
                )
                wr.flush()
                wr.close()

                getResponse(connection) // without it connection will not be closed
            }
        }

        fun getResponse(connection: HttpURLConnection): ByteArray {
            return connection.inputStream.readBytes()
        }
    }
}
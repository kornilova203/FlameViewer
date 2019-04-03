package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.UploadFileUtil.sendFile
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import io.netty.handler.codec.http.HttpResponseStatus
import org.jetbrains.ide.BuiltInServerManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Paths


class DeleteFileTest : LightPlatformCodeInsightFixtureTestCase() {
    fun testDeleteSerFile() {
        PluginFileManager.deleteAllUploadedFiles()
        val projectName = "my-project"
        val fileName = "file.ser"
        val file = createFile(projectName, fileName)
        assertTrue(file.exists())
        assertTrue(file.isFile)

        val newFile = Paths.get(file.parentFile.parentFile.toString(), "uploaded-files", "fierix", file.name).toFile()

        doTestDelete(file, fileName)

        doTestUndoDelete(newFile, fileName)
    }

    fun testDeleteUploadedFile() {
        PluginFileManager.deleteAllUploadedFiles()
        val fileName = "my-flamegraph.flamegraph"
        sendFile(fileName, "fun1;fun2 1".toByteArray())

        val file = PluginFileManager.getLogFile(fileName)
        assertNotNull(file) // file was sent
        assertTrue(file!!.exists())

        doTestDelete(file, fileName, "flamegraph")

        doTestUndoDelete(file, fileName)
    }

    private fun doTestDelete(file: File, fileName: String, converterId: String? = null) {
        assertEquals(HttpResponseStatus.OK.code(), sendDeleteFileRequest(fileName))
        assertFalse(file.exists())

        /* check that file was mode to temp directory for deleted files */
        val fileInDeletedDir = if (converterId != null) {
            Paths.get(PluginFileManager.logDirPath.toString(), "deleted", converterId, fileName).toFile()
        } else {
            Paths.get(PluginFileManager.logDirPath.toString(), "deleted", fileName).toFile()
        }
        assertTrue(fileInDeletedDir.exists())
    }

    private fun doTestUndoDelete(deletedFile: File, fileName: String) {
        val response = sendUndoDeleteRequest(fileName)
        assertEquals(HttpResponseStatus.OK.code(), response)

        assertTrue(deletedFile.exists())
    }

    private fun sendUndoDeleteRequest(fileName: String): Int {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}${ServerNames.UNDO_DELETE_FILE}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        connection.setRequestProperty("File-Name", fileName)

        return connection.responseCode
    }

    private fun sendDeleteFileRequest(fileName: String): Int {
        val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}${ServerNames.DELETE_FILE}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        connection.setRequestProperty("File-Name", fileName)

        return connection.responseCode
    }

    /**
     * Creates file as if it was generated during profiling
     */
    private fun createFile(projectName: String, fileName: String): File {
        val pluginFileManager = PluginFileManager
        pluginFileManager.deleteAllUploadedFiles()

        val projectDir = Paths.get(pluginFileManager.logDirPath.toString(), projectName).toFile()
        projectDir.mkdir()

        val file = Paths.get(projectDir.toString(), fileName).toFile()
        file.outputStream().use {
            it.write("Some text".toByteArray())
        }
        return file
    }
}
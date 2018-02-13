package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.server.FilesUploaderTest
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.jetbrains.ide.BuiltInServerManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL


class HotSpotTest : LightPlatformCodeInsightFixtureTestCase() {
    private val pathToDir = "src/test/resources/com/github/kornilova_l/flamegraph/plugin/server/trees"

    fun testGetHotSpots() {
        PluginFileManager.deleteAllUploadedFiles()
        val file = File("$pathToDir/hot_spots_test.flamegraph")
        FilesUploaderTest.sendFile(file.name, file.readBytes())

        val hotspotsJson = String(sendRequestForHotSpots(file.name))
        assertEquals(File("$pathToDir/hot_spots_test.txt").readText(),
                hotspotsJson.replace("},{", "},\n{"))
    }

    companion object {
        fun sendRequestForHotSpots(fileName: String): ByteArray {
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}" +
                    "/flamegraph-profiler/hot-spots-json?file=$fileName&project=uploaded-files")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            return FilesUploaderTest.getResponse(connection)
        }
    }
}
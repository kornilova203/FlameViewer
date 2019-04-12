package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.server.FILE
import com.github.korniloval.flameviewer.server.NAME
import okhttp3.HttpUrl
import org.jetbrains.ide.BuiltInServerManager
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object UploadFileUtil {
    const val bytesInMB = 1_000_000
    private const val megabytesInOnePart = 20

    fun sendFile(fileName: String, bytes: ByteArray, reverseOrder: Boolean = false) {
        var partsCount = bytes.size / (bytesInMB * megabytesInOnePart)
        if (bytes.size % (bytesInMB * megabytesInOnePart) != 0) {
            partsCount++
        }
        /* do not pause in following block while debugging. It may break connection */
        val list = if (reverseOrder) partsCount - 1 downTo 0 else 0 until partsCount
        for (i in list) {
            //Create connection
            val url = URL("http://localhost:${BuiltInServerManager.getInstance().port}$FILE")
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

            println("$url $fileName ${i + 1}/$partsCount")

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

    fun getUrlBuilderBase(): HttpUrl.Builder = HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(BuiltInServerManager.getInstance().port)
            .addPathSegments(NAME)
}

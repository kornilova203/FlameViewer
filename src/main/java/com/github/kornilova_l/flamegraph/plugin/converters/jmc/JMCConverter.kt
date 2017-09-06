package com.github.kornilova_l.flamegraph.plugin.converters.jmc

import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

class JMCConverter : ProfilerToFlamegraphConverter() {
    override fun isSupported(file: File): Boolean {
        return getFileExtension(file.name) == "jfr"
    }

    override fun convert(file: File): ByteArray {
        val unzippedBytes = getUnzippedBytes(file)
        println(unzippedBytes.size)
        JMCFlightRecorderConverter(ByteArrayInputStream(unzippedBytes)).writeTo(file)
        return getBytes(file)
    }

    private fun getBytes(file: File): ByteArray {
        FileInputStream(file).use {
            return it.readBytes(it.available())
        }
    }

    private fun getUnzippedBytes(file: File): ByteArray {
        FileInputStream(file).use { fileInputStream ->
            try {
                GZIPInputStream(fileInputStream).use { inputStream ->
                    ByteArrayOutputStream().use { bout ->
                        val buffer = ByteArray(1024)
                        var len = inputStream.read(buffer)
                        while (len > 0) {
                            bout.write(buffer, 0, len)
                            len = inputStream.read(buffer)
                        }
                        return bout.toByteArray()
                    }
                }
            } catch (exception: ZipException) {
                return fileInputStream.readBytes(fileInputStream.available())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ByteArray(0)
    }
}
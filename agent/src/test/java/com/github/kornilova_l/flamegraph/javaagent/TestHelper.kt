package com.github.kornilova_l.flamegraph.javaagent

import org.junit.Assert.assertEquals
import java.io.File
import java.io.FileNotFoundException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

fun removePackage(fullName: String): String {
    val dot = fullName.lastIndexOf('.')
    return if (dot == -1) {
        fullName
    } else fullName.substring(dot + 1, fullName.length)
}

fun <T> getBytes(clazz: Class<T>): ByteArray {
    val fullName = clazz.name
    val inputStream = clazz.getResourceAsStream(
            "/" + fullName.replace('.', '/') + ".class")

    return inputStream.use { it.readBytes() }
}

private fun getData(file: File): String {
    try {
        return String(file.readBytes(), Charset.defaultCharset())
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }

    throw RuntimeException("File was not open")
}

fun compareFiles(expected: File, actual: File) {
    assertEquals(getData(expected), getData(actual))
}

fun createDir(name: String) {
    val outFile = File("src/test/resources/" + name)
    if (!outFile.exists()) {
        val res = outFile.mkdir()
        assert(res)
    }
}

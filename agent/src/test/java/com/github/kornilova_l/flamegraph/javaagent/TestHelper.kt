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

val expectedWord = Regex("(?<=\\w)Expected(?= \\{)")

fun compareFiles(expected: File, actual: File, deleteExpectedFile: Boolean = false) {
    val actualBytecode = getData(actual)
    val expectedBytecode = getData(expected).replace(expectedWord, "")
    actual.delete()
    if (deleteExpectedFile) {
        expected.delete()
    }
    assertEquals(expectedBytecode, actualBytecode)
}

fun createDir(name: String) {
    val outFile = File("src/test/resources/" + name)
    if (!outFile.exists()) {
        val res = outFile.mkdir()
        assert(res)
    }
}

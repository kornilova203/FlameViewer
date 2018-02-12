package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.jmc

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.jmc.JMCConverter.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

val bigJfrFile = File("src/test/resources/jfr_files/big_compressed_jmc_5.jfr")
val expectedStacksFile = File("src/test/resources/jfr_files/expected/big_compressed_jmc_5.flamegraph")

class JMCConverterTest {

    @Test
    fun convertLargeFile() {
        val unzippedBytes = getUnzippedBytes(bigJfrFile)
        val newFile = getFileNear(bigJfrFile)
        saveToFile(newFile, unzippedBytes)

        ParseInSeparateProcess.main(arrayOf(newFile.toString()))

        val expected = expectedStacksFile.readText()
        val actual = "${newFile.readText()}\n"

        assertEquals(expected, actual)

        newFile.delete()
    }
}
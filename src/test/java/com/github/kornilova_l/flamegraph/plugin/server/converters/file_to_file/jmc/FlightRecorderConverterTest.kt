package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.jmc

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

private val jmcFive = File("src/test/resources/jfr_files/jmc_5_recording.jfr")
private val jmcFiveCompressed = File("src/test/resources/jfr_files/compressed_jmc_5_recording.jfr")
private val jmcSix = File("src/test/resources/jfr_files/jmc_6_recording.jfr")

class FlightRecorderConverterTest {

    @Test
    fun jmcFiveTest() {
        val converter = FlightRecorderConverter(jmcFive)
        val outputFile = File("temp.jfr")
        converter.writeTo(outputFile)

        assertEquals(
                File("src/test/resources/jfr_files/expected/jmc_5_recording.flamegraph").readText(),
                outputFile.readText()
        )

        outputFile.delete()
    }

    @Test
    fun jmcFiveCompressedTest() {
        val converter = FlightRecorderConverter(jmcFiveCompressed)
        val outputFile = File("temp.jfr")
        converter.writeTo(outputFile)

        assertEquals(
                File("src/test/resources/jfr_files/expected/compressed_jmc_5_recording.flamegraph").readText(),
                outputFile.readText()
        )

        outputFile.delete()
    }

    @Test
    fun jmcSixTest() {
        val converter = FlightRecorderConverter(jmcSix)
        val outputFile = File("temp.jfr")
        converter.writeTo(outputFile)

        assertEquals(
                File("src/test/resources/jfr_files/expected/jmc_6_recording.flamegraph").readText(),
                outputFile.readText()
        )

        outputFile.delete()
    }
}
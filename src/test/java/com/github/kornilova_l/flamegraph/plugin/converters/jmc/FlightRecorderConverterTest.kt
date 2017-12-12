package com.github.kornilova_l.flamegraph.plugin.converters.jmc

import org.junit.Test

import java.io.File

import org.junit.Assert.*

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
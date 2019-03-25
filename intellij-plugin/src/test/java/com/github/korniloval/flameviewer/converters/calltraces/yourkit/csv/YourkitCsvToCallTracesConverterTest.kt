package com.github.korniloval.flameviewer.converters.calltraces.yourkit.csv

import com.github.korniloval.flameviewer.converters.ConverterTestCase

class YourkitCsvToCallTracesConverterTest : ConverterTestCase("csv", "trees/outgoing-calls") {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()

    fun testByThread() = doTest()
}

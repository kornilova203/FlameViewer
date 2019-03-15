package com.github.korniloval.flameviewer.trees.calltraces.yourkit.csv

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class YourkitCsvToCallTracesConverterTest : ConverterTestCase("csv", "trees/outgoing-calls") {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()

    fun testByThread() = doTest()
}

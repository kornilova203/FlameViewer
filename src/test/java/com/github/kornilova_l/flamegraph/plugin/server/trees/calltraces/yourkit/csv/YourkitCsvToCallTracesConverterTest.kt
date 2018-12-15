package com.github.kornilova_l.flamegraph.plugin.server.trees.calltraces.yourkit.csv

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class YourkitCsvToCallTracesConverterTest : ConverterTestCase("csv", "trees/outgoing-calls") {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()

    fun testByThread() = doTest()
}

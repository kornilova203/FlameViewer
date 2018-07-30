package com.github.kornilova_l.flamegraph.plugin.server.trees.call_traces.yourkit_csv

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class YourkitToCallTracesConverterTest : ConverterTestCase("csv", "trees/outgoing-calls") {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()
}

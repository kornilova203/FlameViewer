package com.github.kornilova_l.flamegraph.plugin.server.trees.backtraces.flamegraph

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class FlamegraphToBackTracesTest : ConverterTestCase("flamegraph", "trees/incoming-calls") {
    fun testBiggerTree() {
        doTest()
        doTest(className = "", methodName = "c", description = "")
    }

    fun testMultipleOccurrenceInStack() {
        doTest()
        doTest(className = "", methodName = "a", description = "")
    }
}

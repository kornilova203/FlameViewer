package com.github.kornilova_l.flamegraph.plugin.server.trees.back_traces.flamegraph

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class FlamegraphToBackTracesTest : ConverterTestCase("flamegraph", "trees/incoming-calls") {
    fun testBiggerTree() = doTest(className = "", methodName = "c", description = "")

    fun testMultipleOccurrenceInStack() = doTest(className = "", methodName = "a", description = "")
}

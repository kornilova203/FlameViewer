package com.github.korniloval.flameviewer.converters.backtraces.flamegraph

import com.github.korniloval.flameviewer.converters.ConverterTestCase

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

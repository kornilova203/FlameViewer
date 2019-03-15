package com.github.korniloval.flameviewer.trees.backtraces.flamegraph

import com.github.korniloval.flameviewer.trees.ConverterTestCase

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

package com.github.korniloval.flameviewer.converters.backtraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.BACKTRACES

class FlamegraphToBackTracesTest : ConverterTestCase("flamegraph", BACKTRACES) {
    fun testBiggerTree() {
        doTest()
        doTest(className = "", methodName = "c", description = "")
    }

    fun testMultipleOccurrenceInStack() {
        doTest()
        doTest(className = "", methodName = "a", description = "")
    }
}

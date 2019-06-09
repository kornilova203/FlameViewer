package com.github.korniloval.flameviewer.converters.backtraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.BACKTRACES
import com.github.korniloval.flameviewer.converters.opt

class FlamegraphToBackTracesTest : ConverterTestCase("flamegraph", BACKTRACES) {
    fun testBiggerTree() {
        doTest()
        doTest(opt(className = "", methodName = "c", description = ""))
    }

    fun testMultipleOccurrenceInStack() {
        doTest()
        doTest(opt(className = "", methodName = "a", description = ""))
    }
}

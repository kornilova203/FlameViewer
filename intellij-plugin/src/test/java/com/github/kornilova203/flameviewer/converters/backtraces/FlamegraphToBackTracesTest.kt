package com.github.kornilova203.flameviewer.converters.backtraces

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.BACKTRACES
import com.github.kornilova203.flameviewer.converters.opt

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

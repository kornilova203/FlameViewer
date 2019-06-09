package com.github.korniloval.flameviewer.converters.backtraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType
import com.github.korniloval.flameviewer.converters.opt

class JfrToBackTracesTest : ConverterTestCase("jfr", ResultType.BACKTRACES) {
    fun testCompressedJmc5() {
        doTest()
        doTest(opt(methodName = "getNode", className = "java.util.HashMap", description = "(int, Object)HashMap\$Node"))
        doTest(opt(methodName = "getNode", className = "java.util.HashMap", description = "(int, Object)HashMap\$Node", path = listOf(0, 0, 3)))
        doTest(opt(maxNumOfVisibleNodes = 1))
        doTest(opt(methodName = "getNode", className = "java.util.HashMap", description = "(int, Object)HashMap\$Node", path = listOf(0, 0, 3), maxNumOfVisibleNodes = 1))
    }
}

package com.github.korniloval.flameviewer.converters.backtraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.BACKTRACES
import com.github.korniloval.flameviewer.converters.opt

class FierixToBackTracesTest : ConverterTestCase("fierix", BACKTRACES) {
    fun testTwoThreads() {
        doTest(opt(include = ".*"))
        doTest(opt(include = ".*run"))
        doTest(opt(include = ".*fun.*"))
        doTest(opt(include = ".*fun4"))
    }
}

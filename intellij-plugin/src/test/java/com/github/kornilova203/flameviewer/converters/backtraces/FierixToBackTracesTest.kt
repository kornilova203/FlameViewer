package com.github.kornilova203.flameviewer.converters.backtraces

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.BACKTRACES
import com.github.kornilova203.flameviewer.converters.opt

class FierixToBackTracesTest : ConverterTestCase("fierix", BACKTRACES) {
    fun testTwoThreads() {
        doTest(opt(include = ".*"))
        doTest(opt(include = ".*run"))
        doTest(opt(include = ".*fun.*"))
        doTest(opt(include = ".*fun4"))
    }
}

package com.github.korniloval.flameviewer.converters.backtraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.BACKTRACES

class FierixToBackTracesTest : ConverterTestCase("fierix", BACKTRACES) {
    fun testTwoThreads() {
        doTest(include = ".*")
        doTest(include = ".*run")
        doTest(include = ".*fun.*")
        doTest(include = ".*fun4")
    }
}

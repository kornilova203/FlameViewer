package com.github.korniloval.flameviewer.converters.calltraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTRACES

class YourkitCsvToCallTracesConverterTest : ConverterTestCase("csv", CALLTRACES) {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()

    fun testByThread() = doTest()
}

package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.CALLTRACES

class YourkitCsvToCallTracesConverterTest : ConverterTestCase("csv", CALLTRACES) {

    fun testSimple() = doTest()

    fun testSimple2() = doTest()

    fun testRealData() = doTest()

    fun testByThread() = doTest()
}

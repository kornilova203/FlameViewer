package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.CALLTRACES
import com.github.kornilova203.flameviewer.converters.opt

class FierixToCallTracesTest : ConverterTestCase("fierix", CALLTRACES) {
    fun testClassWithoutPackage() {
        doTest()
        doTest(opt(className = "ClassWithoutPackage", methodName = "fun1", description = "()void"))
    }

    fun testTwoThreads() {
        doTest(opt(include = ".*"))
        doTest(opt(include = ".*run"))
        doTest(opt(include = ".*fun.*"))
        doTest(opt(include = ".*fun4"))
    }
}

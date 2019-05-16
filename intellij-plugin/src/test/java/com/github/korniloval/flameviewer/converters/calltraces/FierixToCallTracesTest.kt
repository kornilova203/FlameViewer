package com.github.korniloval.flameviewer.converters.calltraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTRACES

class FierixToCallTracesTest : ConverterTestCase("fierix", CALLTRACES) {
    fun testClassWithoutPackage() {
        doTest()
        doTest(className = "ClassWithoutPackage", methodName = "fun1", description = "()void")
    }

    fun testTwoThreads() {
        doTest(include = ".*")
        doTest(include = ".*run")
        doTest(include = ".*fun.*")
        doTest(include = ".*fun4")
    }
}

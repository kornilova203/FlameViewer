package com.github.korniloval.flameviewer.converters.calltraces.fierix

import com.github.korniloval.flameviewer.converters.ConverterTestCase

class FierixToCallTracesTest : ConverterTestCase("fierix", "trees/outgoing-calls") {
    fun testClassWithoutPackage() {
        doTest()
        doTest(className = "ClassWithoutPackage", methodName = "fun1", description = "()void")
    }

    fun testTwoThreads() {
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun4")
    }
}

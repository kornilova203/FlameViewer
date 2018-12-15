package com.github.kornilova_l.flamegraph.plugin.server.trees.calltraces.fierix

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

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

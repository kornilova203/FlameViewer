package com.github.kornilova_l.flamegraph.plugin.server.trees.call_traces.ser

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class SerToCallTracesTest : ConverterTestCase("ser", "trees/outgoing-calls") {
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

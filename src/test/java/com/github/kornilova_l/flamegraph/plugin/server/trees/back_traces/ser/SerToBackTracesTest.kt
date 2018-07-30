package com.github.kornilova_l.flamegraph.plugin.server.trees.back_traces.ser

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class SerToBackTracesTest : ConverterTestCase("ser", "trees/incoming-calls") {
    fun testTwoThreads() {
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun4")
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees.backtraces.fierix

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class FierixToBackTracesTest : ConverterTestCase("fierix", "trees/incoming-calls") {
    fun testTwoThreads() {
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun4")
    }
}

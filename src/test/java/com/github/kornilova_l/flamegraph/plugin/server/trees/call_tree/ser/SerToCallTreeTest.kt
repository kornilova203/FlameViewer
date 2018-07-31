package com.github.kornilova_l.flamegraph.plugin.server.trees.call_tree.ser

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class SerToCallTreeTest : ConverterTestCase("ser", "trees/call-tree") {
    fun testFileWithWeirdName() = doTest(fileName = "twoDots.and space")

    fun testSimpleTree() {
        doTest()
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun5")
    }

    fun testTwoThreads() {
        doTest()
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun4")
    }
}

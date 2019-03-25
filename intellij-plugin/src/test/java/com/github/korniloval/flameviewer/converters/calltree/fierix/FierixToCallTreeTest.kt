package com.github.korniloval.flameviewer.converters.calltree.fierix

import com.github.korniloval.flameviewer.converters.ConverterTestCase

class FierixToCallTreeTest : ConverterTestCase("fierix", "trees/call-tree") {
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

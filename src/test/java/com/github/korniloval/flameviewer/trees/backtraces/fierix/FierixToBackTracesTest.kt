package com.github.korniloval.flameviewer.trees.backtraces.fierix

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class FierixToBackTracesTest : ConverterTestCase("fierix", "trees/incoming-calls") {
    fun testTwoThreads() {
        doTest(include = "*")
        doTest(exclude = "*")
        doTest(include = "*run")
        doTest(include = "*fun*")
        doTest(include = "*fun4")
    }
}

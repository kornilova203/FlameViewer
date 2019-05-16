package com.github.korniloval.flameviewer.converters.calltree

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTREE

class FierixToCallTreeTest : ConverterTestCase("fierix", CALLTREE) {
    fun testFileWithWeirdName() = doTest(fileName = "twoDots.and space")

    fun testSimpleTree() {
        doTest()
        doTest(include = ".*")
        doTest(include = ".*run")
        doTest(include = ".*fun.*")
        doTest(include = ".*fun5")
    }

    fun testTwoThreads() {
        doTest()
        doTest(include = ".*")
        doTest(include = ".*run")
        doTest(include = ".*fun.*")
        doTest(include = ".*fun4")
    }
}

package com.github.korniloval.flameviewer.converters.calltree

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTREE
import com.github.korniloval.flameviewer.converters.opt

class FierixToCallTreeTest : ConverterTestCase("fierix", CALLTREE) {
    fun testFileWithWeirdName() = doTest(opt(fileName = "twoDots.and space"))

    fun testSimpleTree() {
        doTest()
        doTest(opt(include = ".*"))
        doTest(opt(include = ".*run"))
        doTest(opt(include = ".*fun.*"))
        doTest(opt(include = ".*fun5"))
    }

    fun testTwoThreads() {
        doTest()
        doTest(opt(include = ".*"))
        doTest(opt(include = ".*run"))
        doTest(opt(include = ".*fun.*"))
        doTest(opt(include = ".*fun4"))
    }
}

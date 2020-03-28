package com.github.kornilova203.flameviewer.converters.calltree

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.CALLTREE
import com.github.kornilova203.flameviewer.converters.opt

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

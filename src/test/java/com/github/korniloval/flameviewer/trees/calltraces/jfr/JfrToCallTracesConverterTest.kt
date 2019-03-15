package com.github.korniloval.flameviewer.trees.calltraces.jfr

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class JfrToCallTracesConverterTest : ConverterTestCase("jfr", "trees/outgoing-calls") {

    fun testJmc5() = doTest()

    fun testJmc6() = doTest()

    fun testCompressedJmc5() = doTest()

    /* jar files cannot be located */
    // fun testBigCompressedJmc5() = doTest()
}

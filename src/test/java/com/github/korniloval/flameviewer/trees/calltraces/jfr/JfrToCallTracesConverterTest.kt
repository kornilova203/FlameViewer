package com.github.korniloval.flameviewer.trees.calltraces.jfr

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class JfrToCallTracesConverterTest : ConverterTestCase("jfr", "trees/outgoing-calls") {

    fun testJmc5() = doTest()

    fun testJmc6() = doTest()

    fun testCompressedJmc5() = doTest()

    /* test file is too big to upload it to git.
     * to get test data add `systemProperty "idea.tests.overwrite.data", "true"`
     * to `test` block in build.gradle and run the test */
    //fun testBigCompressedJmc5() = doTest()
}

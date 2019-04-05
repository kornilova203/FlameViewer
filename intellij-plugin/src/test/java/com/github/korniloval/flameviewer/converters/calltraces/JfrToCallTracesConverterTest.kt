package com.github.korniloval.flameviewer.converters.calltraces

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.CALLTRACES

class JfrToCallTracesConverterTest : ConverterTestCase("jfr", CALLTRACES) {

    fun testJmc5() = doTest()

    fun testJmc6() = doTest()

    fun testCompressedJmc5() = doTest()

    /* test file is too big to upload it to git.
     * to get test data add `systemProperty "idea.tests.overwrite.data", "true"`
     * to `test` block in build.gradle and run the test */
    //fun testBigCompressedJmc5() = doTest()
}

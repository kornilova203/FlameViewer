package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.CALLTRACES
import com.github.kornilova203.flameviewer.converters.opt

class JfrToCallTracesConverterTest : ConverterTestCase("jfr", CALLTRACES) {

    fun testJmc5() = doTest()

    fun testJmc6() = doTest()

    fun testCompressedJmc5() {
        doTest()
        doTest(opt(path = listOf(0, 1, 0, 0, 2)))
        doTest(opt(maxNumOfVisibleNodes = 1, path = listOf(0, 1, 0, 0, 2)))
        doTest(opt(include = ".*(set|<init>|try).*"))
        doTest(opt(include = ".*(set|<init>|try).*", maxNumOfVisibleNodes = 5))
        doTest(opt(include = ".*(set|<init>|try).*", maxNumOfVisibleNodes = 5, path = listOf(0, 0)))
        doTest(opt(className = "com.github.kornilova_l.algorithm_synthesis.grid2D.TwoOrThreeNeighboursKt", methodName = "tryToFindSolutionForEachRulesSet\$default", description = "(List, Writer, int, Object)Set"))
        doTest(opt(className = "com.github.kornilova_l.algorithm_synthesis.grid2D.TwoOrThreeNeighboursKt", methodName = "tryToFindSolutionForEachRulesSet\$default", description = "(List, Writer, int, Object)Set", include = ".*"))
    }

    /* test file is too big to upload it to git.
     * to get test data add `systemProperty "idea.tests.overwrite.data", "true"`
     * to `test` block in build.gradle and run the test */
    //fun testBigCompressedJmc5() = doTest()
}

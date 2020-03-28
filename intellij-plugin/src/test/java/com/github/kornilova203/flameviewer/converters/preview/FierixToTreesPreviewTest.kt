package com.github.kornilova203.flameviewer.converters.preview

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.PREVIEW

class FierixToTreesPreviewTest : ConverterTestCase("fierix", PREVIEW) {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

package com.github.korniloval.flameviewer.converters.preview

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.PREVIEW

class FierixToTreesPreviewTest : ConverterTestCase("fierix", PREVIEW) {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

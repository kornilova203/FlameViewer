package com.github.korniloval.flameviewer.converters.preview.fierix

import com.github.korniloval.flameviewer.converters.ConverterTestCase

class FierixToTreesPreviewTest : ConverterTestCase("fierix", "trees/call-tree/preview") {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

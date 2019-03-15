package com.github.korniloval.flameviewer.trees.preview.fierix

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class FierixToTreesPreviewTest : ConverterTestCase("fierix", "trees/call-tree/preview") {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

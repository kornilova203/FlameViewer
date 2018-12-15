package com.github.kornilova_l.flamegraph.plugin.server.trees.preview.fierix

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class FierixToTreesPreviewTest : ConverterTestCase("fierix", "trees/call-tree/preview") {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

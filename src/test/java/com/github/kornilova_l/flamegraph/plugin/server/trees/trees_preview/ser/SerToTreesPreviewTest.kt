package com.github.kornilova_l.flamegraph.plugin.server.trees.trees_preview.ser

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class SerToTreesPreviewTest : ConverterTestCase("ser", "trees/call-tree/preview") {
    fun testSimpleTree() = doTest()

    fun testTwoThreads() = doTest()
}

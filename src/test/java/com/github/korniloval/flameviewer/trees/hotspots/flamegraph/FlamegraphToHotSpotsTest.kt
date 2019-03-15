package com.github.korniloval.flameviewer.trees.hotspots.flamegraph

import com.github.korniloval.flameviewer.trees.ConverterTestCase

class FlamegraphToHotSpotsTest : ConverterTestCase("flamegraph", "hot-spots-json") {
    fun testSimple() = doTest()
}

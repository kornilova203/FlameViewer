package com.github.kornilova_l.flamegraph.plugin.server.trees.hotspots.flamegraph

import com.github.kornilova_l.flamegraph.plugin.server.trees.ConverterTestCase

class FlamegraphToHotSpotsTest : ConverterTestCase("flamegraph", "hot-spots-json") {
    fun testSimple() = doTest()
}

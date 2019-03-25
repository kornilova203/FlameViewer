package com.github.korniloval.flameviewer.converters.hotspots.flamegraph

import com.github.korniloval.flameviewer.converters.ConverterTestCase

class FlamegraphToHotSpotsTest : ConverterTestCase("flamegraph", "hot-spots-json") {
    fun testSimple() = doTest()
}

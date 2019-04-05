package com.github.korniloval.flameviewer.converters.hotspots

import com.github.korniloval.flameviewer.converters.ConverterTestCase
import com.github.korniloval.flameviewer.converters.ResultType.HOTSPOTS

class FlamegraphToHotSpotsTest : ConverterTestCase("flamegraph", HOTSPOTS) {
    fun testSimple() = doTest()
}

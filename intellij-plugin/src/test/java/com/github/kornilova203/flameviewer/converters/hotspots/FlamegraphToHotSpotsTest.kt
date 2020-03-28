package com.github.kornilova203.flameviewer.converters.hotspots

import com.github.kornilova203.flameviewer.converters.ConverterTestCase
import com.github.kornilova203.flameviewer.converters.ResultType.HOTSPOTS

class FlamegraphToHotSpotsTest : ConverterTestCase("flamegraph", HOTSPOTS) {
    fun testSimple() = doTest()
}

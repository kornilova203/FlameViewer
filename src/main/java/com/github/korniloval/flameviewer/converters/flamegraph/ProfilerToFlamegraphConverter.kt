package com.github.korniloval.flameviewer.converters.flamegraph

import com.github.korniloval.flameviewer.converters.Converter

@Deprecated("Implement ProfilerToCFlamegraphConverter instead")
interface ProfilerToFlamegraphConverter : Converter<Map<String, Int>> {
    override fun convert(): Map<String, Int>
}

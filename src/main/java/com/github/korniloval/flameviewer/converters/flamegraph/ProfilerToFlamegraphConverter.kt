package com.github.korniloval.flameviewer.converters.flamegraph

@Deprecated("Implement ProfilerToCFlamegraphConverter instead")
interface ProfilerToFlamegraphConverter {
    fun convert(): Map<String, Int>
}

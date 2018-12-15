package com.github.kornilova_l.flamegraph.plugin.server.converters.flamegraph

@Deprecated("Implement ProfilerToCFlamegraphConverter instead")
interface ProfilerToFlamegraphConverter {
    fun convert(): Map<String, Int>
}

package com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph

/**
 * @author Liudmila Kornilova
 **/
interface ProfilerToCFlamegraphConverter {
    fun convert(): CFlamegraph
}

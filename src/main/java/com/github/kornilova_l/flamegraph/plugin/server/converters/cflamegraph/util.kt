package com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph

import java.util.HashMap

fun toArray(names: HashMap<String, Int>): Array<String> {
    val array = Array(names.size) { "" }
    for (entry in names) {
        array[entry.value] = entry.key
    }
    return array
}

fun getId(map: HashMap<String, Int>, name: String): Int {
    val id = map[name]
    if (id == null) {
        val newId = map.size
        map[name] = newId
        return newId
    }
    return id
}

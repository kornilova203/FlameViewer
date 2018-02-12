package com.github.kornilova_l.flamegraph.plugin.server.trees.util


class UniqueStringsKeeper {
    private val uniqueStrings = HashMap<String, String>()
    val size: Int
        get() = uniqueStrings.size

    fun getUniqueString(string: String): String {
        /* computeIfAbsent acquires lock */
        val existingString = uniqueStrings[string]
        if (existingString != null) {
            return existingString
        }
        uniqueStrings[string] = string
        return string
    }
}
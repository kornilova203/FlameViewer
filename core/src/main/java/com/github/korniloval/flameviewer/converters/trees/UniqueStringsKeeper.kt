package com.github.korniloval.flameviewer.converters.trees


class UniqueStringsKeeper {
    private val uniqueStrings = HashMap<String, String>()
    val size: Int
        get() = uniqueStrings.size

    fun getUniqueString(string: String): String {
        return uniqueStrings.putIfAbsent(string, string) ?: string
    }
}
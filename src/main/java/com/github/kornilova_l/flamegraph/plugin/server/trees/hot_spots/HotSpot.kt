package com.github.kornilova_l.flamegraph.plugin.server.trees.hot_spots


class HotSpot internal constructor(tempHotSpot: TempHotSpot) {
    private val className: String
    private val methodName: String
    private val parameters: Array<String>
    private val retVal: String
    internal val relativeTime: Float

    init {
        val desc = tempHotSpot.desc
        val openBracketPos = desc.indexOf('(')
        val closeBracketPos = desc.indexOf(')')
        if (closeBracketPos != -1 && openBracketPos != -1 && openBracketPos < closeBracketPos) {
            val params = desc.substring(openBracketPos + 1, closeBracketPos)
            parameters = params.split(", ".toRegex()).toTypedArray()
            retVal = desc.substring(closeBracketPos + 1, desc.length)
        } else {
            parameters = arrayOf()
            retVal = ""
        }
        className = tempHotSpot.className
        methodName = tempHotSpot.methodName
        relativeTime = tempHotSpot.relativeTime
    }
}
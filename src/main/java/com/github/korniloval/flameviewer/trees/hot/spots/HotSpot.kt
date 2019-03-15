package com.github.korniloval.flameviewer.trees.hot.spots


class HotSpot internal constructor(tempHotSpot: TempHotSpot) {
    private val className: String
    private val methodName: String
    /**
     * if method does not have a description then parameters array is empty.
     * If method has a description without parameters then the array has one empty string.
     * This differentiation is needed to correctly restore original description on client side.
     */
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
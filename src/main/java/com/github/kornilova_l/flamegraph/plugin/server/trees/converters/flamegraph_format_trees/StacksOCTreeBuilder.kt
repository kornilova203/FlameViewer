package com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees

/**
 * Builds tree in which methods contain parameters
 * and maybe contain return value
 */
class StacksOCTreeBuilder(stacks: Map<String, Int>) : SimpleStacksOCTreeBuilder(stacks) {

    override fun getDescription(call: String): String {
        return call.substring(call.indexOf("("), call.indexOf(")") + 1) + getRetType(call)
    }

    override fun getMethodName(call: String): String {
        /* find last dot before parameters */
        var lastDot = -1
        for (i in 0 until call.length) {
            if (call[i] == '(') {
                break
            } else if (call[i] == '.') {
                lastDot = i
            }
        }
        if (lastDot != -1) {
            return call.substring(lastDot + 1, call.indexOf('('))
        }
        throw IllegalArgumentException("Method does not contain return value")
    }

    override fun getClassName(call: String): String {
        /* find last dot before parameters */
        var lastDot = -1
        for (i in 0 until call.length) {
            if (call[i] == '(') {
                break
            } else if (call[i] == '.') {
                lastDot = i
            }
        }
        if (lastDot != -1) {
            /* if contains return value */
            for (i in 0 until lastDot) {
                if (call[i] == ' ') {
                    return call.substring(i + 1, lastDot)
                }
            }
            return call.substring(0, lastDot)
        }
        throw IllegalArgumentException("Method does not contain return value")
    }

    private fun getRetType(call: String): String {
        val space = call.indexOf(' ')
        val openBracket = call.indexOf('(')
        return if (space != -1 && space < openBracket) { // if space exist and it is not in parameters
            call.substring(0, space)
        } else {
            ""
        }
    }
}

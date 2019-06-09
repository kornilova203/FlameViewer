package com.github.korniloval.flameviewer.converters.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.korniloval.flameviewer.FlameLogger
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class Filter(private val include: Pattern) {

    fun isIncluded(node: Node): Boolean = isIncluded(getNodeString(node))

    private fun isIncluded(nodeString: String): Boolean = include.matcher(nodeString).matches()

    private fun getNodeString(node: Node): String {
        val nodeInfo = node.nodeInfo
        return nodeInfo.className + nodeInfo.methodName
    }

    companion object {
        fun tryCreate(includePattern: String, logger: FlameLogger, ignoreParseErrors: Boolean): Filter? {
            return try {
                Filter(Pattern.compile(includePattern))
            } catch (e: PatternSyntaxException) {
                if (!ignoreParseErrors) logger.warn("Failed to compile filter pattern", e)
                null
            }
        }
    }
}

package com.github.korniloval.flameviewer.converters.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import com.github.korniloval.flameviewer.FlameLogger
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class Filter(include: String?, exclude: String?, private val logger: FlameLogger) {
    private val include: Pattern?
    private val exclude: Pattern?

    init {
        this.include = compilePattern(include)
        this.exclude = compilePattern(exclude)
    }

    private fun compilePattern(patternString: String?): Pattern? {
        if (patternString != null) {
            return try {
                Pattern.compile(
                        patternString.replace(".", """\.""")
                                .replace("*", ".*")
                                .replace("$", """\$""")
                )
            } catch (e: PatternSyntaxException) {
                logger.warn("Failed to compile filter pattern", e)
                null
            }
        }
        return null
    }

    fun isNodeIncluded(node: Node): Boolean {
        val nodeString = getNodeString(node)
        return isIncluded(nodeString) && !isExcluded(nodeString)
    }

    private fun isIncluded(nodeString: String): Boolean {
        if (include == null) {
            return true
        }
        return include.matcher(nodeString).matches()
    }

    private fun isExcluded(nodeString: String): Boolean {
        if (exclude == null) {
            return false
        }
        return exclude.matcher(nodeString).matches()
    }


    private fun getNodeString(node: Node): String {
        val nodeInfo = node.nodeInfo
        return nodeInfo.className + nodeInfo.methodName
    }
}

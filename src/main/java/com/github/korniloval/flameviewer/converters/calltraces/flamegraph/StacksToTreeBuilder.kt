package com.github.korniloval.flameviewer.converters.calltraces.flamegraph

import com.github.korniloval.flameviewer.trees.TreeBuilder
import com.github.korniloval.flameviewer.trees.util.TreesUtil
import com.github.korniloval.flameviewer.trees.util.TreesUtil.setNodesCount
import com.github.korniloval.flameviewer.trees.util.TreesUtil.setNodesOffsetRecursively
import com.github.korniloval.flameviewer.trees.util.TreesUtil.updateNodeList
import com.github.korniloval.flameviewer.trees.util.UniqueStringsKeeper
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree


class StacksToTreeBuilder(stacks: Map<String, Int>) : TreeBuilder {
    private val treeBuilder: Tree.Builder = Tree.newBuilder()
    private val tree: Tree
    private var maxDepth = 0
    private val uniqueStrings = UniqueStringsKeeper()

    init {
        tree = buildTree(stacks)
    }

    private fun buildTree(stacks: Map<String, Int>): Tree {
        treeBuilder.setBaseNode(Tree.Node.newBuilder())
        processStacks(stacks)
        setNodesOffsetRecursively(treeBuilder.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(treeBuilder)
        setNodesCount(treeBuilder)
        treeBuilder.depth = maxDepth
        return treeBuilder.build()
    }

    private fun processStacks(stacks: Map<String, Int>) {
        for (stack in stacks.entries) {
            addStackToTree(stack)
        }
    }

    private fun addStackToTree(stack: Map.Entry<String, Int>) {
        val width = stack.value
        val calls = stack.key.split(";".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (calls.size > maxDepth) {
            maxDepth = calls.size
        }
        var nodeBuilder: Tree.Node.Builder = treeBuilder.baseNodeBuilder

        for (call in calls) {
            val openBracketPos = call.indexOf('(')
            val parametersPos = if (openBracketPos == -1) call.length else openBracketPos // call.length if no parameters
            val lastSpacePosBeforeParams = getLastSpacePosBeforeParams(call, parametersPos) // -1 if no space

            nodeBuilder = updateNodeList(nodeBuilder,
                    uniqueStrings.getUniqueString(getClassName(call, parametersPos, lastSpacePosBeforeParams) ?: ""),
                    uniqueStrings.getUniqueString(getMethodName(call, parametersPos)),
                    uniqueStrings.getUniqueString(getDescription(call, parametersPos, lastSpacePosBeforeParams)
                            ?: ""), width.toLong())
        }
    }

    override fun getTree(): Tree? {
        return tree
    }

    companion object {
        fun getLastSpacePosBeforeParams(name: String, openBracketPos: Int): Int {
            for (i in openBracketPos - 1 downTo 0) {
                if (name[i] == ' ') {
                    return i
                }
            }
            return -1
        }

        /**
         * We do not know if name contains return value.
         * It may even not contain class name
         */
        fun getClassName(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
            var lastDot = -1
            for (i in parametersPos - 1 downTo 0) {
                if (name[i] == '.') {
                    lastDot = i
                    break
                }
            }
            if (lastDot == -1) {
                return null
            }
            return name.substring(lastSpacePosBeforeParams + 1, lastDot)
        }

        fun getDescription(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
            return if (parametersPos == name.length && lastSpacePosBeforeParams == -1) {
                null
            } else if (lastSpacePosBeforeParams == -1) { // if only parameters
                name.substring(parametersPos, name.length)
            } else if (parametersPos == name.length) { // if only ret val
                "()" + name.substring(0, lastSpacePosBeforeParams)
            } else { // if both
                name.substring(parametersPos, name.length) + name.substring(0, lastSpacePosBeforeParams)
            }
        }

        fun getMethodName(name: String, parametersPos: Int): String {
            for (i in parametersPos - 1 downTo 0) {
                val c = name[i]
                if (c == '.' || c == ' ') {
                    return name.substring(i + 1, parametersPos)
                }
            }
            return name.substring(0, parametersPos)
        }
    }
}

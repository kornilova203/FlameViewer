package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph.Converter.Companion.getClassName
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph.Converter.Companion.getDescription
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph.Converter.Companion.getLastSpacePosBeforeParams
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph.Converter.Companion.getMethodName
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.setNodesCount
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.setNodesOffsetRecursively
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.updateNodeList
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.UniqueStringsKeeper
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
            val parametersPos = if (openBracketPos == -1) call.length else openBracketPos
            val lastSpacePosBeforeParams = getLastSpacePosBeforeParams(call, parametersPos)

            nodeBuilder = updateNodeList(nodeBuilder,
                    uniqueStrings.getUniqueString(getClassName(call, parametersPos, lastSpacePosBeforeParams)),
                    uniqueStrings.getUniqueString(getMethodName(call, parametersPos)),
                    uniqueStrings.getUniqueString(getDescription(call, parametersPos, lastSpacePosBeforeParams)), width.toLong())
        }
    }

    override fun getTree(): Tree? {
        return tree
    }
}

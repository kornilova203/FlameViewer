package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getClassName
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getDescription
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getLastSpacePosBeforeParams
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getMethodName
import com.github.korniloval.flameviewer.converters.trees.TreeBuilder
import com.github.korniloval.flameviewer.converters.trees.TreesUtil
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.setNodesCount
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.setNodesOffsetRecursively
import com.github.korniloval.flameviewer.converters.trees.TreesUtil.updateNodeList
import com.github.korniloval.flameviewer.converters.trees.UniqueStringsKeeper


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
        val calls = stack.key.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
}

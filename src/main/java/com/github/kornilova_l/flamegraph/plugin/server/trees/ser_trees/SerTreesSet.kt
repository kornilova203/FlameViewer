package com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees

import com.github.kornilova_l.flamegraph.plugin.server.trees.Filter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager.TreeType
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.call_tree.CallTreesBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreePreviewBuilder
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.call_traces.OutgoingCallsBuilder
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos.Trees

import java.io.File

class SerTreesSet(logFile: File) : TreesSet() {

    init {
        callTree = CallTreesBuilder(logFile).trees
    }

    override fun getTreesPreview(filter: Filter?): TreesPreview? {
        val callTree = getCallTree(filter) ?: return null
        return TreePreviewBuilder(callTree).treesPreview
    }

    override fun getTree(treeType: TreeType,
                         filter: Filter?): Tree? {
        if (callTree == null) {
            return null
        }
        if (callTraces == null) {
            callTraces = OutgoingCallsBuilder(callTree!!).tree
        }
        return getTreeMaybeFilter(treeType, filter)
    }

    override fun getCallTree(filter: Filter?): Trees? {
        if (callTree == null) {
            return null
        }
        if (filter == null) {
            return callTree
        }
        val filteredTrees = Trees.newBuilder()
        for (tree in callTree!!.treesList) {
            val filteredTree = filterTree(tree, filter, true)
            if (filteredTree != null) {
                filteredTrees.addTrees(filteredTree)
            }
        }
        return if (filteredTrees.treesCount == 0) {
            null
        } else filteredTrees.build()
    }

    override fun getCallTree(filter: Filter?, threadsIds: List<Int>?): Trees? {
        val trees = getCallTree(filter) ?: return null
        if (threadsIds == null) {
            return trees
        }
        val wantedTrees = Trees.newBuilder()
        for (threadsId in threadsIds) {
            wantedTrees.addTrees(trees.getTrees(threadsId))
        }
        return wantedTrees.build()
    }
}

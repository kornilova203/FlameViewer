package com.github.korniloval.flameviewer.trees

import com.github.korniloval.flameviewer.trees.util.TreePreviewBuilder
import com.github.korniloval.flameviewer.trees.util.accumulative.trees.call.traces.CallTreeToCallTracesConverter
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos
import com.github.kornilova_l.flamegraph.proto.TreesProtos

class TreesSetImpl : TreesSet {
    constructor(callTraces: TreeProtos.Tree) {
        this.callTraces = callTraces
    }

    constructor(callTree: TreesProtos.Trees) {
        /* call traces will be computed when requested */
        this.callTree = callTree
    }

    override fun getTreesPreview(filter: Filter?): TreesPreviewProtos.TreesPreview? {
        val callTree = getCallTree(filter) ?: return null
        return TreePreviewBuilder(callTree).treesPreview
    }

    override fun getTree(treeType: TreeManager.TreeType, filter: Filter?): TreeProtos.Tree? {
        if (callTraces == null) {
            callTraces = CallTreeToCallTracesConverter(callTree!!).tree
        }
        return when (treeType) {
            TreeManager.TreeType.BACK_TRACES -> getBackTracesMaybeFiltered(filter)
            TreeManager.TreeType.CALL_TRACES -> getCallTracesMaybeFiltered(filter)
        }
    }

    override fun getCallTree(filter: Filter?): TreesProtos.Trees? {
        if (callTree == null) {
            return null
        }
        if (filter == null) {
            return callTree
        }
        val filteredTrees = TreesProtos.Trees.newBuilder()
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

    override fun getCallTree(filter: Filter?, threadsIds: List<Int>?): TreesProtos.Trees? {
        val trees = getCallTree(filter) ?: return null
        if (threadsIds == null) {
            return trees
        }
        val wantedTrees = TreesProtos.Trees.newBuilder()
        for (threadsId in threadsIds) {
            wantedTrees.addTrees(trees.getTrees(threadsId))
        }
        return wantedTrees.build()
    }
}

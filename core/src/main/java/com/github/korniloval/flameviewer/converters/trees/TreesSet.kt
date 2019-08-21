package com.github.korniloval.flameviewer.converters.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.trees.TreeType.BACK_TRACES
import com.github.korniloval.flameviewer.converters.trees.TreeType.CALL_TRACES
import com.github.korniloval.flameviewer.converters.trees.backtraces.BackTracesBuilder
import com.github.korniloval.flameviewer.converters.trees.backtraces.BackTracesMethodBuilder
import com.github.korniloval.flameviewer.converters.trees.hotspots.HotSpot
import com.github.korniloval.flameviewer.converters.trees.hotspots.HotSpotsBuilder
import java.util.*


abstract class TreesSet {
    private var hotSpots: ArrayList<HotSpot>? = null
    protected var callTree: TreesProtos.Trees? = null
    protected var callTraces: Tree? = null
    private var backTraces: Tree? = null

    abstract fun getTreesPreview(filter: Filter?): TreesPreview?

    abstract fun getTree(treeType: TreeType): Tree?

    fun getTree(treeType: TreeType,
                className: String,
                methodName: String,
                desc: String): Tree? {
        val callTraces = getTree(CALL_TRACES) ?: return null
        return when (treeType) {
            CALL_TRACES -> CallTracesMethodBuilder(callTraces, className, methodName, desc).tree
            BACK_TRACES -> BackTracesMethodBuilder(callTraces, className, methodName, desc).tree
        }
    }

    abstract fun getCallTree(filter: Filter?): TreesProtos.Trees?

    abstract fun getCallTree(filter: Filter?, threadsIds: List<Int>?): TreesProtos.Trees?

    fun getHotSpots(): List<HotSpot> {
        var hotSpots = hotSpots
        if (hotSpots == null) {
            val callTraces = getTree(CALL_TRACES) ?: return ArrayList()
            hotSpots = HotSpotsBuilder(callTraces).hotSpots
        }
        this.hotSpots = hotSpots
        return hotSpots
    }

    protected fun getBackTraces(): Tree? {
        val callTraces = callTraces ?: return null
        return if (backTraces != null) backTraces!! else BackTracesBuilder(callTraces).tree
    }
}

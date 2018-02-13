package com.github.kornilova_l.flamegraph.plugin.server.trees.hot_spots

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class HotSpotsBuilder(callTraces: Tree) {
    val hotSpots = ArrayList<HotSpot>()

    init {
        val hotSpotTreeMap = HashMap<TempHotSpot, TempHotSpot>()
        for (node in callTraces.baseNode.nodesList) { // avoid baseNode
            getHotSpotsRecursively(node, hotSpotTreeMap, callTraces)
        }
        for (tempHotSpot in hotSpotTreeMap.values) {
            hotSpots.add(HotSpot(tempHotSpot))
        }
        hotSpots.sortWith(Comparator { hotSpot1, hotSpot2 -> java.lang.Float.compare(hotSpot2.relativeTime, hotSpot1.relativeTime) })
    }

    private fun getHotSpotsRecursively(node: Tree.Node, hotSpotTreeMap: HashMap<TempHotSpot, TempHotSpot>, callTraces: Tree) {
        var hotSpot = TempHotSpot(
                node.nodeInfo.className,
                node.nodeInfo.methodName,
                node.nodeInfo.description
        )
        hotSpot = hotSpotTreeMap.putIfAbsent(hotSpot, hotSpot) ?: hotSpot
        hotSpot.addTime(getSelfTime(node).toFloat() / callTraces.width)
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            getHotSpotsRecursively(child, hotSpotTreeMap, callTraces)
        }
    }

    private fun getSelfTime(node: Tree.Node): Long {
        var childTime: Long = 0
        for (i in 0 until node.nodesList.size) {
            val child = node.nodesList[i]
            childTime += child.width
        }
        return node.width - childTime
    }
}

/**
 * [HotSpot] class splits description to list of parameters.
 * We do not want to do it when we compute hotspots
 */
internal class TempHotSpot(val className: String, val methodName: String, val desc: String, var relativeTime: Float = 0f) {

    fun addTime(time: Float) {
        relativeTime += time
    }

    override fun hashCode(): Int {
        return Objects.hash(className, methodName, desc)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TempHotSpot) {
            return false
        }
        return className == other.className &&
                methodName == other.methodName &&
                desc == other.desc
    }
}
package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.FlameIndicator
import com.github.korniloval.flameviewer.converters.Converter
import com.github.korniloval.flameviewer.converters.FramesParsingUtil
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getClassName
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getDescription
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getMethodName
import java.util.*

class StacksToCFlamegraphConverter(private val stacks: Map<String, Int>) : Converter<CFlamegraph> {

    override fun convert(indicator: FlameIndicator?): CFlamegraph {
        val cflamegraphLines = ArrayList<CFlamegraphLine>()
        val classNames = HashMap<String, Int>()
        val methodNames = HashMap<String, Int>()
        val descriptions = HashMap<String, Int>()

        val stacksList = stacks.entries.mapTo(ArrayList()) { Pair(it.key, it.value) }
        stacksList.sortBy { it.first }
        val splitStacks = stacksList.map {
            indicator?.checkCanceled()
            Pair(splitAndRemoveThreadId(it.first), it.second)
        }

        splitStacks.forEachChild(NodeCoordinates(0, splitStacks.size, -1)) { childCoord ->
            indicator?.checkCanceled()
            buildCFlamegraph(splitStacks, childCoord, cflamegraphLines, classNames, methodNames, descriptions)
        }

        return CFlamegraph(
                cflamegraphLines,
                toArray(classNames),
                toArray(methodNames),
                toArray(descriptions)
        )
    }

    private fun splitAndRemoveThreadId(stack: String): List<String> {
        val stacks = stack.split(";")
        if (stacks.size > 1 && stacks[0].startsWith('[') && stacks[0].endsWith(']')) {
            // async-profiler thread id
            return stacks.subList(1, stacks.size)
        }
        return stacks
    }

    /**
     * Depth first search
     */
    private fun buildCFlamegraph(stacks: List<Pair<List<String>, Int>>, coordinates: NodeCoordinates, cflamegraphLines: ArrayList<CFlamegraphLine>,
                                 classNames: HashMap<String, Int>, methodNames: HashMap<String, Int>, descriptions: HashMap<String, Int>) {

        val node = buildCFlamegraphLine(stacks, coordinates, classNames, methodNames, descriptions)
        if (node != null) cflamegraphLines.add(node)

        stacks.forEachChild(coordinates) { childCoordinates ->
            buildCFlamegraph(stacks, childCoordinates, cflamegraphLines, classNames, methodNames, descriptions)
        }
    }

    private fun buildCFlamegraphLine(stacks: List<Pair<List<String>, Int>>, coordinates: NodeCoordinates, classNames: HashMap<String, Int>,
                                     methodNames: HashMap<String, Int>, descriptions: HashMap<String, Int>): CFlamegraphLine? {
        val frame = stacks[coordinates.start].first[coordinates.depth]
        val openBracketPos = frame.indexOf('(')
        val parametersPos = if (openBracketPos == -1) frame.length else openBracketPos // call.length if no parameters
        val lastSpacePosBeforeParams = FramesParsingUtil.getLastSpacePosBeforeParams(frame, parametersPos) // -1 if no space
        val className = getClassName(frame, parametersPos, lastSpacePosBeforeParams)?.replace('/', '.')
        val classNameId = if (className == null) null else getId(classNames, className)
        val methodName = getMethodName(frame, parametersPos)
        val methodNameId = getId(methodNames, methodName)
        val desc = getDescription(frame, parametersPos, lastSpacePosBeforeParams)
        val descId = if (desc == null) null else getId(descriptions, desc)

        val width = calcWidth(stacks, coordinates.start, coordinates.end)
        if (width == 0) return null
        return CFlamegraphLine(
                classNameId,
                methodNameId,
                descId,
                width,
                coordinates.depth + 1
        )
    }

    private fun calcWidth(stacks: List<Pair<List<String>, Int>>, start: Int, end: Int): Int {
        var width = 0
        for (i in start until end) {
            width += stacks[i].second
        }
        return width
    }

    private fun List<Pair<List<String>, Int>>.forEachChild(coordinates: NodeCoordinates, processor: (NodeCoordinates) -> Unit) {
        var firstExistingNode: Int? = null
        val childDepth = coordinates.depth + 1

        for (currentStackNumber in coordinates.start until coordinates.end) {
            if (childDepth < this[currentStackNumber].first.size) {
                firstExistingNode = currentStackNumber
                break
            }
        }
        if (firstExistingNode == null) return

        var childStart: Int = firstExistingNode
        var currentFrame: String? = this[firstExistingNode].first[childDepth]
        for (currentStackNumber in firstExistingNode + 1 until coordinates.end) {
            val stack = this[currentStackNumber].first
            val frame = if (childDepth < stack.size) stack[childDepth] else null
            if (frame != currentFrame) {
                if (currentFrame != null) processor(NodeCoordinates(childStart, currentStackNumber, childDepth))
                childStart = currentStackNumber
                currentFrame = frame
            }
        }
        if (currentFrame != null) processor(NodeCoordinates(childStart, coordinates.end, childDepth))
    }

    private data class NodeCoordinates(val start: Int, val end: Int, val depth: Int)
}

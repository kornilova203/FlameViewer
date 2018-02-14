package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph

import com.github.kornilova_l.flamegraph.plugin.pleaseReportIssue
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.parsePositiveInt
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.parsePositiveLong
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern


internal class Converter(file: File) {
    val tree: TreeProtos.Tree
    private val classNames = HashMap<Int, String>()
    private val methodNames = HashMap<Int, String>()
    private val descriptions = HashMap<Int, String>()
    private val headerPattern = Pattern.compile("--[CMD]-- \\d+")
    private var maxDepth = 0

    init {
        val tree = createEmptyTree()
        val currentStack = ArrayList<TreeProtos.Tree.Node.Builder>()
        currentStack.add(tree.baseNodeBuilder)
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line: String? = initMaps(reader)
            while (line != null) {
                if (line.isNotBlank()) {
                    processLine(line, currentStack)
                }
                line = reader.readLine()
            }
        }
        tree.depth = maxDepth
        TreesUtil.setNodesOffsetRecursively(tree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(tree)
        TreesUtil.setNodesCount(tree)
        this.tree = tree.build()
    }

    private fun initMaps(reader: BufferedReader): String {
        var line = reader.readLine()
        while (line.isEmpty() || headerPattern.matcher(line).matches()) {
            if (line.isEmpty()) {
                line = reader.readLine()
                continue
            }
            val linesCount = Integer.parseInt(line.substring(6))
            val value = line[2]
            val currentMap = when (value) {
                'C' -> classNames
                'M' -> methodNames
                'D' -> descriptions
                else ->
                    /* this should never happen because we checked line with regexp */
                    throw IllegalArgumentException("$pleaseReportIssue: Cannot read header $line")
            }
            for (i in 0 until linesCount) {
                val mapLine = reader.readLine()
                val lastSpacePos = mapLine.lastIndexOf(' ')
                val id = parsePositiveInt(mapLine, lastSpacePos + 1, mapLine.length)
                val name = mapLine.substring(0, lastSpacePos)
                currentMap[id] = name
            }
            line = reader.readLine()
        }
        return line
    }

    private fun processLine(line: String,
                            currentStack: ArrayList<TreeProtos.Tree.Node.Builder>) {
        var className: String? = null
        var methodName: String? = null
        var desc: String? = null
        var width = -1L
        var depth = -1
        var i = 0
        while (i < line.length - 1) {
            val c = line[i]
            val endOfNumPos = getNextEndOfNum(line, i + 2)
            when (c) {
                'C' -> className = classNames[getParamIntValue(line, i, endOfNumPos)]!!
                'M' -> methodName = methodNames[getParamIntValue(line, i, endOfNumPos)]!!
                'D' -> desc = descriptions[getParamIntValue(line, i, endOfNumPos)]!!
                'd' -> depth = getParamIntValue(line, i, endOfNumPos)
                'w' -> width = getParamLongValue(line, i, endOfNumPos)
            }
            i = endOfNumPos
        }
        if (depth == -1 || width == -1L) {
            throw IllegalArgumentException("$pleaseReportIssue: Cannot find depth or width value in line: $line")
        }
        if (methodName == null) {
            throw IllegalArgumentException("$pleaseReportIssue: Line must contain method name: $line")
        }
        while (depth < currentStack.size) { // if some calls are finished
            currentStack.removeAt(currentStack.size - 1)
        }
        val newNode = TreesUtil.updateNodeList(
                currentStack[currentStack.size - 1],
                className ?: "",
                methodName,
                desc ?: "",
                width
        )
        currentStack.add(newNode)
        if (currentStack.size - 1 > maxDepth) {
            maxDepth = currentStack.size - 1
        }
    }

    private fun getParamIntValue(line: String, paramPos: Int, endOfNumPos: Int): Int {
        return parsePositiveInt(line, paramPos + 1, endOfNumPos)
    }

    private fun getParamLongValue(line: String, paramPos: Int, endOfNumPos: Int): Long {
        return parsePositiveLong(line, paramPos + 1, endOfNumPos)
    }

    private fun getNextEndOfNum(line: String, startIndex: Int): Int {
        for (i in startIndex until line.length) {
            if (line[i] !in '0'..'9') {
                return i
            }
        }
        return line.length
    }

    private fun createEmptyTree(): TreeProtos.Tree.Builder {
        val tree = TreeProtos.Tree.newBuilder()
        tree.setBaseNode(TreeProtos.Tree.Node.newBuilder())
        return tree
    }
}
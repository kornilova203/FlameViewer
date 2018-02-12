package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph

import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.UniqueStringsKeeper
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


internal class CompressedFlamegraphConverter(file: File) {
    val tree: TreeProtos.Tree
    private val uniqueStringsClassName = UniqueStringsKeeper()
    private val uniqueStringsMethodName = UniqueStringsKeeper()
    private val uniqueStringsDesc = UniqueStringsKeeper()
    var maxDepth = 0

    init {
        val tree = createEmptyTree()
        val currentStack = ArrayList<TreeProtos.Tree.Node.Builder>()
        currentStack.add(tree.baseNodeBuilder)
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line = reader.readLine()
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

    private fun processLine(line: String,
                            currentStack: ArrayList<TreeProtos.Tree.Node.Builder>) {
        val lastSpacePos = line.lastIndexOf(' ')
        val newDepth = Integer.parseInt(line.substring(lastSpacePos + 1, line.length))
        val secondSpacePos = getNextSpacePos(line, lastSpacePos)
        val width = java.lang.Long.parseLong(line.substring(secondSpacePos + 1, lastSpacePos))

        val name = line.substring(0, secondSpacePos)
        while (newDepth < currentStack.size) { // if some calls are finished
            currentStack.removeAt(currentStack.size - 1)
        }
        val openBracketPos = name.indexOf('(')
        val parametersPos = if (openBracketPos == -1) name.length else openBracketPos
        val newNode = TreesUtil.updateNodeList(
                currentStack[currentStack.size - 1],
                uniqueStringsClassName.getUniqueString(getClassName(name, parametersPos)),
                uniqueStringsMethodName.getUniqueString(getMethodName(name, parametersPos)),
                uniqueStringsDesc.getUniqueString(getDescription(name, parametersPos)),
                width
        )
        currentStack.add(newNode)
        if (currentStack.size - 1 > maxDepth) {
            maxDepth = currentStack.size - 1
        }
    }

    private fun getNextSpacePos(line: String, prevSpacePos: Int): Int {
        for (i in prevSpacePos - 1 downTo 0) {
            if (line[i] == ' ') {
                return i
            }
        }
        return -1
    }

    private fun getDescription(name: String, parametersPos: Int): String {
        val parameters = name.substring(parametersPos, name.length)
        for (i in parametersPos - 1 downTo 0) {
            if (name[i] == ' ') { // if contains return value
                return parameters + name.substring(0, i)
            }
        }
        return parameters
    }

    /**
     * We do not know if name contains return value.
     * It may even not contain class name
     */
    private fun getClassName(name: String, parametersPos: Int): String {
        var lastDot = -1
        var spacePos = -1
        for (i in parametersPos - 1 downTo 0) {
            if (name[i] == '.') {
                lastDot = i
            } else if (name[i] == ' ') {
                spacePos = i
            }
        }
        if (lastDot == -1) {
            return ""
        }
        return if (spacePos == -1) {
            name.substring(0, lastDot)
        } else {
            name.substring(spacePos + 1, lastDot)
        }
    }

    private fun getMethodName(name: String, parametersPos: Int): String {
        for (i in parametersPos - 1 downTo 0) {
            val c = name[i]
            if (c == '.' || c == ' ') {
                return name.substring(i + 1, parametersPos)
            }
        }
        return name.substring(0, parametersPos)
    }

    private fun createEmptyTree(): TreeProtos.Tree.Builder {
        val tree = TreeProtos.Tree.newBuilder()
        tree.setBaseNode(TreeProtos.Tree.Node.newBuilder())
        return tree
    }
}
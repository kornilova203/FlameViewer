package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.setTreeWidth
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.accumulative_trees.AccumulativeTreesHelper.setNodesOffsetRecursively
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class YourkitCsvToCallTracesConverter : FileToCallTracesConverter() {
    override fun getId(): String {
        return "yourkit"
    }

    override fun isSupported(file: File): Boolean {
        if (ProfilerToFlamegraphConverter.getFileExtension(file.name) != "csv") {
            return false
        }
        BufferedReader(FileReader(file)).use { reader ->

            reader.readLine() // skip header
            reader.readLine() // skip header

            var line: String? = reader.readLine()

            while (line != null) {
                val parts = line.split("\",\"")
                if (parts.size != 3) {
                    return false
                }
                val firstString = parts[0].removePrefix("\"")
                if (!isMethod(firstString) && !firstString.contains(':')) {
                    return false
                }
                try {
                    Integer.parseInt(parts[1]) // time
                    Integer.parseInt(parts[2].removeSuffix("\"")) // level
                } catch (e: NumberFormatException) {
                    return false
                }

                line = reader.readLine()
            }
        }
        return true
    }

    private fun isMethod(s: String): Boolean {
        val openBracketPos = s.indexOf('(')
        val closeBracketPos = s.indexOf(')')
        return openBracketPos != -1 &&
                closeBracketPos != -1 &&
                openBracketPos < closeBracketPos
    }

    override fun convert(file: File): TreeProtos.Tree {
        val tree = createEmptyTree()
        val currentStack = ArrayList<Node.Builder>()
        var maxDepth = 0
        currentStack.add(tree.baseNodeBuilder)
        file.forEachLine { line ->
            val values = line.split("\",\"")
            if (values[0].contains('(')) {
                val time = getTime(values)
                val newDepth = getDepth(values) - 1 // first call has depth 1
                val name = getName(values)
                while (newDepth <= currentStack.size - 1) { // if some calls are finished
                    currentStack.removeAt(currentStack.size - 1)
                }
                val newNode = AccumulativeTreesHelper.updateNodeList(currentStack[currentStack.size - 1], getClassName(name),
                        getMethodName(name), getDescription(name), time)
                currentStack.add(newNode)
                if (currentStack.size - 1 > maxDepth) {
                    maxDepth = currentStack.size - 1
                }
            }
        }
        tree.depth = maxDepth
        setNodesOffsetRecursively(tree.baseNodeBuilder, 0)
        setTreeWidth(tree)
        return tree.build()
    }

    private fun getDescription(name: String): String {
        return name.substring(name.indexOf('('), name.length)
    }

    private fun getClassName(name: String): String {
        val parametersPos = name.indexOf('(')
        var lastDot = 0
        for (i in 0 until parametersPos) {
            if (name[i] == '.') {
                lastDot = i
            }
        }
        if (lastDot == 0) {
            return ""
        }
        return name.substring(0, lastDot)
    }

    private fun getMethodName(name: String): String {
        val parametersPos = name.indexOf('(')
        var lastDot = -1
        for (i in 0 until parametersPos) {
            if (name[i] == '.') {
                lastDot = i
            }
        }
        return name.substring(lastDot + 1, parametersPos)
    }

    private fun createEmptyTree(): TreeProtos.Tree.Builder {
        val tree = TreeProtos.Tree.newBuilder()
        tree.setBaseNode(Node.newBuilder())
        return tree
    }

    private fun getName(values: List<String>): String {
        val name = values[0].removePrefix("\"")
        val openBracketPos = name.lastIndexOf('(')
        val lastSpacePos = name.substring(0, openBracketPos).lastIndexOf(' ') // remove parameters because they may contain spaces
        return name.substring(lastSpacePos + 1, name.length)
    }

    private fun getDepth(values: List<String>): Int {
        return Integer.parseInt(values[2].removeSuffix("\""))
    }

    private fun getTime(values: List<String>): Long {
        return java.lang.Long.parseLong(values[1])
    }
}

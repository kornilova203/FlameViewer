package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.setNodesOffsetRecursively
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.UniqueStringsKeeper
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree.Node
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

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

    override fun convert(file: File): Tree = Converter(file).tree
}

private class Converter(file: File) {
    val tree: Tree
    val uniqueStringsClassName = UniqueStringsKeeper()
    val uniqueStringsMethodName = UniqueStringsKeeper()
    val uniqueStringsDesc = UniqueStringsKeeper()
    var maxDepth = 0

    init {
        val tree = createEmptyTree()
        val currentStack = ArrayList<Node.Builder>()
        currentStack.add(tree.baseNodeBuilder)
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                processLine(line, currentStack)
                line = reader.readLine()
            }
        }
        tree.depth = maxDepth
        setNodesOffsetRecursively(tree.baseNodeBuilder, 0)
        TreesUtil.setTreeWidth(tree)
        TreesUtil.setNodesCount(tree)
        this.tree = tree.build()
    }

    private fun processLine(line: String,
                            currentStack: ArrayList<Node.Builder>) {
        val delimPos = line.indexOf("\",\"")
        if (delimPos == -1) {
            return
        }
        var name = line.substring(1, delimPos) // remove prefix '"'
        if (!name.contains('(')) {
            return
        }
        var time = -1L
        var newDepth = -1
        try {
            /* find next delimiter */
            for (i in delimPos + 1 until line.length - 2) {
                if (line[i] == '"' && line[i + 1] == ',' && line[i + 2] == '"') {
                    time = java.lang.Long.parseLong(line.substring(delimPos + 3, i))
                    newDepth = Integer.parseInt(line.substring(i + 3, line.length - 1))
                    break
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        if (time == -1L || newDepth == -1) {
            return
        }
        newDepth -= 1 // after this depth of first call is 1
        name = getCleanName(name)
        while (newDepth <= currentStack.size - 1) { // if some calls are finished
            currentStack.removeAt(currentStack.size - 1)
        }
        val parametersPos = name.indexOf('(')
        val newNode = TreesUtil.updateNodeList(
                currentStack[currentStack.size - 1],
                uniqueStringsClassName.getUniqueString(getClassName(name, parametersPos)),
                uniqueStringsMethodName.getUniqueString(getMethodName(name, parametersPos)),
                uniqueStringsDesc.getUniqueString(getDescription(name, parametersPos)),
                time
        )
        currentStack.add(newNode)
        if (currentStack.size - 1 > maxDepth) {
            maxDepth = currentStack.size - 1
        }
    }

    private fun getDescription(name: String, parametersPos: Int): String {
        return name.substring(parametersPos, name.length)
    }

    private fun getClassName(name: String, parametersPos: Int): String {
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

    private fun getMethodName(name: String, parametersPos: Int): String {
        var lastDot = -1
        for (i in 0 until parametersPos) {
            if (name[i] == '.') {
                lastDot = i
            }
        }
        return name.substring(lastDot + 1, parametersPos)
    }

    private fun createEmptyTree(): Tree.Builder {
        val tree = Tree.newBuilder()
        tree.setBaseNode(Node.newBuilder())
        return tree
    }

    private fun getCleanName(name: String): String {
        val openBracketPos = name.lastIndexOf('(')
        val lastSpacePos = name.substring(0, openBracketPos).lastIndexOf(' ') // remove parameters because they may contain spaces
        return name.substring(lastSpacePos + 1, name.length)
    }
}

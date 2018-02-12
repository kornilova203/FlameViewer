package com.github.kornilova_l.flamegraph.plugin.server.trees.converters

import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.UniqueStringsKeeper
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


/**
 * Original flamegraph format consumes a lot of memory on disk and it takes more time to parse
 * because it has lots of duplicate parts of call traces.
 * cflamegraph (compressed flamegraph) solves this problem.
 *
 * Each line of cflamegraph contains information in following format:
 * method-call width depth
 *
 * where method-call line may contain any characters (also spaces)
 * width and depth are integers
 *
 * Example:
 * ._ _
 * |c|d|___ _
 * |b______|e|_______ _
 * |a________________|f|
 *
 * a 100 1
 * b 40 2
 * c 5 3
 * d 5 3
 * e 5 2
 * f 5 1
 *
 * As you can see order of lines matters
 * because if a call has bigger depth than previous it means that
 * the call is a child of previous call.
 *
 * In original flamegraph format this example would look like this:
 * a;b;c 5
 * a;b;d 5
 * a;b 10
 * a;e 5
 * a 50
 * f 5
 * it has 2 times more call names that compact version of flamegraph.
 */
class CompressedFlamegraphToCallTracesConverter : FileToCallTracesConverter() {
    val extension = "cflamegraph"

    override fun getId(): String = extension

    /**
     * Each line must contain non-space characters and two numbers.
     * I do not use patterns here because they are slow
     */
    override fun isSupported(file: File): Boolean {
        if (ProfilerToFlamegraphConverter.getFileExtension(file.name) != extension) {
            return false
        }
        BufferedReader(FileReader(file)).use { reader ->

            var line: String? = reader.readLine()

            while (line != null) {
                if (line.isNotBlank()) {
                    val startPosOfTwoNumbers = getStartPosOfTwoNumbers(line)
                    if (startPosOfTwoNumbers == -1) { // must contain two numbers
                        return false
                    }
                    var containsNonSpaceChar = false
                    for (i in 0 until startPosOfTwoNumbers) { // must contain non-space characters before two numbers
                        if (line[i] != ' ') {
                            containsNonSpaceChar = true // if contains then we can go to next line
                            break
                        }
                    }
                    if (!containsNonSpaceChar) {
                        return false
                    }
                }
                line = reader.readLine()
            }
        }
        return true
    }

    override fun convert(file: File): Tree = CompressedFlamegraphConverter(file).tree

    private fun getStartPosOfTwoNumbers(line: String): Int {
        var numbersCount = 0
        var wasNumberPresent = false
        for (i in line.length - 1 downTo 0) {
            val c = line[i]
            if (c == ' ' && wasNumberPresent) {
                numbersCount++
                wasNumberPresent = false
                if (numbersCount == 2) {
                    return i + 1
                }
            } else if (c in '0'..'9') {
                wasNumberPresent = true
            } else {
                return -1
            }
        }
        return -1
    }
}

private class CompressedFlamegraphConverter(file: File) {
    val tree: Tree
    val uniqueStringsClassName = UniqueStringsKeeper()
    val uniqueStringsMethodName = UniqueStringsKeeper()
    val uniqueStringsDesc = UniqueStringsKeeper()
    var maxDepth = 0

    init {
        val tree = createEmptyTree()
        val currentStack = ArrayList<Tree.Node.Builder>()
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
                            currentStack: ArrayList<Tree.Node.Builder>) {
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

    private fun createEmptyTree(): Tree.Builder {
        val tree = Tree.newBuilder()
        tree.setBaseNode(Tree.Node.newBuilder())
        return tree
    }
}
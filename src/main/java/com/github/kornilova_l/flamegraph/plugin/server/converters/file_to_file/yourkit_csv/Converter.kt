package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.yourkit_csv

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.flamegraph.StacksToTreeBuilder
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.CFlamegraph
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.CFlamegraphLine
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.parsePositiveInt
import com.github.kornilova_l.flamegraph.plugin.server.trees.util.TreesUtil.parsePositiveLong
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


class Converter(file: File) {
    private val cFlamegraphLines = ArrayList<CFlamegraphLine>()
    private val classNames = HashMap<String, Int>()
    private val methodNames = HashMap<String, Int>()
    private val descriptions = HashMap<String, Int>()
    val cFlamegraph: CFlamegraph

    init {
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                processLine(line)
                line = reader.readLine()
            }
        }
        cFlamegraph = CFlamegraph(cFlamegraphLines, classNames, methodNames, descriptions)
    }

    private fun processLine(line: String) {
        val delimPos = line.indexOf("\",\"")
        if (delimPos == -1) {
            return
        }
        var name = line.substring(1, delimPos) // remove prefix '"'
        if (!name.contains('(')) {
            return
        }
        var width = -1L
        var depth = -1
        try {
            /* find next delimiter */
            for (i in delimPos + 1 until line.length - 2) {
                if (line[i] == '"' && line[i + 1] == ',' && line[i + 2] == '"') {
                    width = parsePositiveLong(line, delimPos + 3, i)
                    depth = parsePositiveInt(line, i + 3, line.length - 1)
                    break
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        if (width == -1L || depth == -1) {
            return
        }
        depth -= 1 // after this depth of first call is 1
        name = getCleanName(name)
        val openBracketPos = name.indexOf('(')
        val parametersPos = if (openBracketPos == -1) name.length else openBracketPos
        val lastSpacePosBeforeParams = StacksToTreeBuilder.getLastSpacePosBeforeParams(name, parametersPos)
        val className = StacksToTreeBuilder.getClassName(name, parametersPos, lastSpacePosBeforeParams)
        val methodName = StacksToTreeBuilder.getMethodName(name, parametersPos)
        val desc = StacksToTreeBuilder.getDescription(name, parametersPos, lastSpacePosBeforeParams)
        cFlamegraphLines.add(
                CFlamegraphLine(
                        if (className == null) null else getId(classNames, className),
                        getId(methodNames, methodName),
                        if (desc == null) null else getId(descriptions, desc),
                        width,
                        depth
                )
        )
    }

    private fun getId(map: HashMap<String, Int>, name: String): Int {
        val id = map[name]
        if (id == null) {
            val newId = map.size
            map[name] = newId
            return newId
        }
        return id
    }

    private fun getCleanName(name: String): String {
        val openBracketPos = name.lastIndexOf('(')
        val lastSpacePos = name.substring(0, openBracketPos).lastIndexOf(' ') // remove parameters because they may contain spaces
        return name.substring(lastSpacePos + 1, name.length)
    }
}
package com.github.korniloval.flameviewer.converters.cflamegraph.yourkit.csv

import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getClassName
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getDescription
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getLastSpacePosBeforeParams
import com.github.korniloval.flameviewer.converters.FramesParsingUtil.getMethodName
import com.github.korniloval.flameviewer.converters.cflamegraph.*
import com.github.korniloval.flameviewer.trees.util.TreesUtil.parsePositiveInt
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


class YourkitCsvToCFlamegraphConverter(private val file: File) : ToCFlamegraphConverter {
    private val cFlamegraphLines = ArrayList<CFlamegraphLine>()
    private val classNames = HashMap<String, Int>()
    private val methodNames = HashMap<String, Int>()
    private val descriptions = HashMap<String, Int>()

    override fun convert(): CFlamegraph {
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                processLine(line)
                line = reader.readLine()
            }
        }
        return CFlamegraph(cFlamegraphLines,
                toArray(classNames),
                toArray(methodNames),
                toArray(descriptions))
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
        var width = -1
        var depth = -1
        try {
            /* find next delimiter */
            for (i in delimPos + 1 until line.length - 2) {
                if (line[i] == '"' && line[i + 1] == ',' && line[i + 2] == '"') {
                    width = parsePositiveInt(line, delimPos + 3, i)
                    depth = parsePositiveInt(line, i + 3, line.length - 1)
                    break
                }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        if (width == -1 || depth == -1) {
            return
        }
        depth -= 1 // after this depth of first call is 1
        name = getCleanName(name)
        val openBracketPos = name.indexOf('(')
        val parametersPos = if (openBracketPos == -1) name.length else openBracketPos
        val lastSpacePosBeforeParams = getLastSpacePosBeforeParams(name, parametersPos)
        val className = getClassName(name, parametersPos, lastSpacePosBeforeParams)
        val methodName = getMethodName(name, parametersPos)
        val desc = getDescription(name, parametersPos, lastSpacePosBeforeParams)
        if (width > 0) {
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
    }

    private fun getCleanName(name: String): String {
        val openBracketPos = name.lastIndexOf('(')
        val lastSpacePos = name.substring(0, openBracketPos).lastIndexOf(' ') // remove parameters because they may contain spaces
        return name.substring(lastSpacePos + 1, name.length)
    }
}
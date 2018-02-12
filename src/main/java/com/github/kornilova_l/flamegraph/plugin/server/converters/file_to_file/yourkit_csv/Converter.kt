package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.yourkit_csv

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.CFlamegraphLine
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*


class Converter(file: File) {
    val cFlamegraphLines = ArrayList<CFlamegraphLine>()

    init {
        BufferedReader(FileReader(file), 1000 * 8192).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                processLine(line)
                line = reader.readLine()
            }
        }
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
                    width = java.lang.Long.parseLong(line.substring(delimPos + 3, i))
                    depth = Integer.parseInt(line.substring(i + 3, line.length - 1))
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
        cFlamegraphLines.add(CFlamegraphLine(name, width, depth))

    }

    private fun getCleanName(name: String): String {
        val openBracketPos = name.lastIndexOf('(')
        val lastSpacePos = name.substring(0, openBracketPos).lastIndexOf(' ') // remove parameters because they may contain spaces
        return name.substring(lastSpacePos + 1, name.length)
    }
}
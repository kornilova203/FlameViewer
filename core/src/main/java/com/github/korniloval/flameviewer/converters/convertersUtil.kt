package com.github.korniloval.flameviewer.converters

import com.github.korniloval.flameviewer.FlameLogger
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.regex.Pattern

object FramesParsingUtil {
    fun getLastSpacePosBeforeParams(name: String, openBracketPos: Int): Int {
        for (i in openBracketPos - 1 downTo 0) {
            if (name[i] == ' ') {
                return i
            }
        }
        return -1
    }

    /**
     * @param lastSpacePosBeforeParams is -1 if there is no last space
     * We do not know if name contains return value.
     * It may even not contain class name
     */
    fun getClassName(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
        var lastDot = -1
        for (i in parametersPos - 1 downTo Math.max(0, lastSpacePosBeforeParams)) {
            if (name[i] == '.') {
                lastDot = i
                break
            }
        }
        if (lastDot == -1) {
            return null
        }
        return name.substring(lastSpacePosBeforeParams + 1, lastDot)
    }

    fun getDescription(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
        return if (parametersPos == name.length && lastSpacePosBeforeParams == -1) {
            null
        } else if (lastSpacePosBeforeParams == -1) { // if only parameters
            name.substring(parametersPos, name.length)
        } else if (parametersPos == name.length) { // if only ret val
            "()" + name.substring(0, lastSpacePosBeforeParams)
        } else { // if both
            name.substring(parametersPos, name.length) + name.substring(0, lastSpacePosBeforeParams)
        }
    }

    fun getMethodName(name: String, parametersPos: Int): String {
        for (i in parametersPos - 1 downTo 0) {
            val c = name[i]
            if (c == '.' || c == ' ') {
                return name.substring(i + 1, parametersPos)
            }
        }
        return name.substring(0, parametersPos)
    }
}

fun getBytes(file: File, logger: FlameLogger): ByteArray? {
    try {
        FileInputStream(file).use { stream ->
            return IOUtils.toByteArray(stream)
        }
    } catch (e: IOException) {
        logger.error("Failed to read bytes from file", e)
    }
    return null
}

private val F_LINE_PATTERN = Pattern.compile("[^\\s].* \\d+")
private const val F_LINES_TO_CHECK = 10_000

/**
 * @return true if there is at least one flamegraph line
 */
fun isFlamegraph(file: File, logger: FlameLogger): Boolean {
    val bytes = getBytes(file, logger) ?: return false
    try {
        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes))).use { reader ->
            var line: String? = reader.readLine()
            var i = 0
            while (line != null && i < F_LINES_TO_CHECK) {
                if (F_LINE_PATTERN.matcher(line).matches()) return true
                line = reader.readLine()
                i++
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return false
}

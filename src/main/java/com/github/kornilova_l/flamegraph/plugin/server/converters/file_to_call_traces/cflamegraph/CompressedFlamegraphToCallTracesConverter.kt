package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.cflamegraph

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_traces.FileToCallTracesConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


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
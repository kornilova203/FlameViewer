package com.github.kornilova_l.flamegraph.plugin.converters.jmc

import com.jrockit.mc.common.IMCFrame
import com.jrockit.mc.flightrecorder.FlightRecording
import com.jrockit.mc.flightrecorder.FlightRecordingLoader
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace
import java.io.*
import java.util.*

/**
 * ParserFlightRecorderConverter takes .jfr unzippedFile
 * Converts it to [FlameGraph](https://github.com/brendangregg/FlameGraph) format
 * Saves to /stacks dir in profiler dir
 */
class JMCFlightRecorderConverter constructor(inputSteam : InputStream) {
    private val stacks = HashMap<String, Int>()

    init {
        println("start converting")
        val recording = FlightRecordingLoader.loadStream(inputSteam)
        println("get recording")
        buildStacks(recording)
    }

    private fun buildStacks(recording: FlightRecording) {
        val view = recording.createView()
        view
                .filter {
                    // Filter for Method Profiling Sample Events
                    EVENT_TYPE == it.eventType.name
                }
                .map {
                    // long eventStartTimestamp = event.getStartTimestamp();
                    // long eventEndTimestamp = event.getEndTimestamp();
                    // Get Stack Trace from the event. Field ID was identified from
                    // event.getEventType().getFieldIdentifiers()
                    it.getValue(EVENT_VALUE_STACK) as FLRStackTrace
                }
                .map { getStack(it) }
                .forEach { processStack(it) }
    }

    private fun processStack(stack: Stack<String>) {
        val stackTraceBuilder = StringBuilder()
        var appendSemicolon = false
        while (!stack.empty()) {
            if (appendSemicolon) {
                stackTraceBuilder.append(";")
            } else {
                appendSemicolon = true
            }
            stackTraceBuilder.append(stack.pop())
        }
        val stackTrace = stackTraceBuilder.toString()
        var count: Int? = stacks[stackTrace]
        if (count == null) {
            count = 1
        } else {
            count++
        }
        stacks.put(stackTrace, count)
    }

    fun writeTo(file: File) {
        try {
            BufferedWriter(FileWriter(file)).use { bufferedWriter ->
                for ((key, value) in stacks) {
                    bufferedWriter.write(String.format("%s %d%n", key, value))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        private val EVENT_TYPE = "Method Profiling Sample"
        private val EVENT_VALUE_STACK = "(stackTrace)"
        private val showReturnValue = true
        private val useSimpleNames = false
        private val hideArguments = false
        private val ignoreLineNumbers = true

        private fun getStack(flrStackTrace: FLRStackTrace): Stack<String> {
            val stack = Stack<String>()
            for (frame in flrStackTrace.frames) {
                // Push method to a stack
                stack.push(getFrameName(frame))
            }
            return stack
        }

        private fun getFrameName(frame: IMCFrame): String {
            val methodBuilder = StringBuilder()
            val method = frame.method
            methodBuilder.append(method.getHumanReadable(showReturnValue, !useSimpleNames, true, !useSimpleNames,
                    !hideArguments, !useSimpleNames))
            if (!ignoreLineNumbers) {
                methodBuilder.append(":")
                methodBuilder.append(frame.frameLineNumber)
            }
            return methodBuilder.toString()
        }
    }
}
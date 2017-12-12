package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.util.Stack;

/**
 * ParserFlightRecorderConverter takes .jfr unzippedFile
 * Converts it to [FlameGraph](https://github.com/brendangregg/FlameGraph) format
 * Saves to /stacks dir in profiler dir
 */
@SuppressWarnings("FieldCanBeLocal")
class FlightRecorderConverterEight extends Converter {
    private static String EVENT_TYPE = "Method Profiling Sample";
    private static String EVENT_VALUE_STACK = "(stackTrace)";
    private static boolean showReturnValue = true;
    private static boolean useSimpleNames = false;
    private static boolean hideArguments = false;

    FlightRecorderConverterEight(InputStream inputStream) {
        System.out.println("Start");
        FlightRecording recording = FlightRecordingLoader.loadStream(inputStream);
        System.out.println("Got Recording");
        buildStacks(recording);
        System.out.println("Built stacks");
    }

    FlightRecorderConverterEight(File file) {
        System.out.println("Start");
        FlightRecording recording = FlightRecordingLoader.loadFile(file);
        System.out.println("Got Recording");
        buildStacks(recording);
        System.out.println("Built stacks");
    }

    private static Stack<String> getStack(FLRStackTrace flrStackTrace) {
        Stack<String> stack = new Stack<>();
        for (IMCFrame frame : flrStackTrace.getFrames()) {
            // Push method to a stack
            if (frame != null) {
                stack.push(getFrameName(frame));
            }
        }
        return stack;
    }

    private static String getFrameName(@NotNull IMCFrame frame) {
        StringBuilder methodBuilder = new StringBuilder();
        IMCMethod method = frame.getMethod();
        methodBuilder.append(method.getHumanReadable(showReturnValue, !useSimpleNames, true, !useSimpleNames,
                !hideArguments, !useSimpleNames));
        return methodBuilder.toString();
    }

    private void buildStacks(FlightRecording recording) {
        IView view = recording.createView();
        for (IEvent event : view) {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName())) {
                FLRStackTrace flrStackTrace = (FLRStackTrace) event.getValue(EVENT_VALUE_STACK);
                Stack<String> stack = getStack(flrStackTrace);
                processStack(stack);
            }
        }
    }

    private void processStack(Stack<String> stack) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        boolean appendSemicolon = false;
        while (!stack.empty()) {
            if (appendSemicolon) {
                stackTraceBuilder.append(";");
            } else {
                appendSemicolon = true;
            }
            stackTraceBuilder.append(stack.pop());
        }
        String stackTrace = stackTraceBuilder.toString();
        Integer count = stacks.get(stackTrace);
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        stacks.put(stackTrace, count);
    }
}
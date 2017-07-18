package com.github.kornilova_l.flamegraph.plugin.server.trees.jfr_trees;

import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

/**
 * FlightRecorderConverter takes .jfr file
 * Converts it to <a href="https://github.com/brendangregg/FlameGraph">FlameGraph</a> format
 * Saves to /stacks dir in profiler dir
 */
public class FlightRecorderConverter {
    private static final String EVENT_TYPE = "Method Profiling Sample";
    private static final String EVENT_VALUE_STACK = "(stackTrace)";
    private static final boolean showReturnValue = true;
    private static final boolean useSimpleNames = false;
    private static final boolean hideArguments = false;
    private static final boolean ignoreLineNumbers = true;
    private final Map<String, Integer> stacks = new HashMap<>();

    public FlightRecorderConverter(@NotNull File file) throws IllegalArgumentException {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist");
        }
        if (!Objects.equals(getExtension(file), "jfr")) {
            throw new IllegalArgumentException("Wrong file extension");
        }
        FlightRecording recording = getRecording(file);
        buildStacks(recording);
    }

    @NotNull
    private static FlightRecording getRecording(@NotNull File file) {
        try (GZIPInputStream gzipStream = new GZIPInputStream(new FileInputStream(file))) {
            return FlightRecordingLoader.loadStream(gzipStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("File cannot be opened");
        }
    }

    @NotNull
    private static String getExtension(@NotNull File file) {
        int dot = file.getName().lastIndexOf(".");
        if (dot != -1) {
            return file.getName().substring(dot + 1, file.getName().length());
        }
        return "";
    }

    private static Stack<String> getStack(FLRStackTrace flrStackTrace) {
        Stack<String> stack = new Stack<>();
        for (IMCFrame frame : flrStackTrace.getFrames()) {
            // Push method to a stack
            stack.push(getFrameName(frame));
        }
        return stack;
    }

    private static String getFrameName(IMCFrame frame) {
        StringBuilder methodBuilder = new StringBuilder();
        IMCMethod method = frame.getMethod();
        methodBuilder.append(method.getHumanReadable(showReturnValue, !useSimpleNames, true, !useSimpleNames,
                !hideArguments, !useSimpleNames));
        if (!ignoreLineNumbers) {
            methodBuilder.append(":");
            methodBuilder.append(frame.getFrameLineNumber());
        }
        return methodBuilder.toString();
    }

    public Map<String, Integer> getStacks() {
        return stacks;
    }

    private void buildStacks(FlightRecording recording) {
        IView view = recording.createView();
        for (IEvent event : view) {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName())) {
//                long eventStartTimestamp = event.getStartTimestamp();
//                long eventEndTimestamp = event.getEndTimestamp();

                // Get Stack Trace from the event. Field ID was identified from
                // event.getEventType().getFieldIdentifiers()
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

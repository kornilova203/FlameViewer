package com.github.kornilova_l.flamegraph.plugin.server.jfr_converter;

import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ParserFlightRecorderConverter takes .jfr file
 * Converts it to <a href="https://github.com/brendangregg/FlameGraph">FlameGraph</a> format
 * Saves to /stacks dir in profiler dir
 */
public class JMCFlightRecorderConverter {
    private static final String EVENT_TYPE = "Method Profiling Sample";
    private static final String EVENT_VALUE_STACK = "(stackTrace)";
    private static final boolean showReturnValue = true;
    private static final boolean useSimpleNames = false;
    private static final boolean hideArguments = false;
    private static final boolean ignoreLineNumbers = true;
    private final Map<String, Integer> stacks = new HashMap<>();

    public JMCFlightRecorderConverter(File unzippedFile) throws IllegalArgumentException {
        System.out.println("start converting");
        FlightRecording recording = FlightRecordingLoader.loadFile(unzippedFile);
        System.out.println("get recording");
        buildStacks(recording);
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

    public void writeTo(File file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
                bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new JMCFlightRecorderConverter(
                new File("/home/lk/Downloads/flight_recording_180121comintellijideaMain17940.jfr")
        ).writeTo(
                new File("/home/lk/Downloads/flight_recording_180121comintellijideaMain14552.jfr.converted")
        );
    }
}
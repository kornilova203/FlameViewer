package com.github.kornilova_l.flamegraph.plugin.converters.jmc;

import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * ParserFlightRecorderConverter takes .jfr unzippedFile
 * Converts it to [FlameGraph](https://github.com/brendangregg/FlameGraph) format
 * Saves to /stacks dir in profiler dir
 */
@SuppressWarnings("FieldCanBeLocal")
class JMCFlightRecorderConverter {
    private static String EVENT_TYPE = "Method Profiling Sample";
    private static String EVENT_VALUE_STACK = "(stackTrace)";
    private static boolean showReturnValue = true;
    private static boolean useSimpleNames = false;
    private static boolean hideArguments = false;
    private Map<String, Integer> stacks = new HashMap<>();
    JMCFlightRecorderConverter(InputStream inputStream) {
        System.out.println("Start");
        FlightRecording recording = FlightRecordingLoader.loadStream(inputStream);
        System.out.println("Got Recording");
        buildStacks(recording);
        System.out.println("Built stacks");
    }

    private JMCFlightRecorderConverter(File file) {
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
            stack.push(getFrameName(frame));
        }
        return stack;
    }

    private static String getFrameName(IMCFrame frame) {
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

    public void writeTo(File file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, Integer> entry : stacks.entrySet()) {
                bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("Specify path to file");
            return;
        }
        System.out.println(args[0]);
        File file = new File(args[0]);
        System.out.println(file);
        new JMCFlightRecorderConverter(file).writeTo(file);

    }
}
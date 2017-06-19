package com.github.kornilova_l.profiler.logger;

import com.github.kornilova_l.protos.EventProtos;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread which writes all events from loggingQueue to file
 */
public class Logger implements Runnable {
    public static final LinkedBlockingDeque<EventData> queue = new LinkedBlockingDeque<>();
    private static final File outDir = new File("out");
    private static final File file = createOutFile();
    // stream is package-private because it will be closed by WaitingLoggingToFinish thread
    static final OutputStream outputStream;
    static boolean isWriting = false;

    static {
        OutputStream temp = null;
        try {
            temp = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        outputStream = temp;
        createDirIfNotExist(outDir);
    }

    private static int getLargestFileNum() {
        File[] files = outDir.listFiles();
        int max = 0;
        if (files != null) {
            Pattern getNumPattern = Pattern.compile("[0-9]+");
            OptionalInt optionalMax = Arrays.stream(files)
                    .map(File::getName) // get names of files
                    .map((name) -> {
                        Matcher m = getNumPattern.matcher(name);
                        if (m.find()) {
                            return m.group(); // get numbers from fileNames
                        }
                        return "0";
                    })
                    .mapToInt(Integer::parseInt)
                    .max();

            if (optionalMax.isPresent()) {
                max = optionalMax.getAsInt();
            }
        }
        return max;
    }

    private static File createOutFile() {
        int max = getLargestFileNum();
        return new File(outDir.getAbsolutePath() + "/events" + ++max + ".ser");
    }

    private static void createDirIfNotExist(File outDir) {
        if (!outDir.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                outDir.mkdir();
            } catch (SecurityException se) {
                //handle it
            }
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                logEvent(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void logEvent(EventData eventData) {
        isWriting = true;
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder()
                .setTime(eventData.time)
                .setThreadId(eventData.threadId);
        if (eventData.getClass() == EnterEventData.class) {
            eventBuilder.setEnter(
                    formEnterMessage((EnterEventData) eventData)
            );
        } else {
            eventBuilder.setExit(
                    formExitMessage((ExitEventData) eventData)
            );
        }
        writeToFile(eventBuilder.build());
        isWriting = false;
    }

    private static EventProtos.Event.Exit formExitMessage(ExitEventData exitEventData) {
        EventProtos.Event.Exit.Builder exitBuilder = EventProtos.Event.Exit.newBuilder();
        if (exitEventData.returnValue != null) {
            exitBuilder.setReturnValue(objectToVar(exitEventData.returnValue));
        }
        return exitBuilder.build();
    }

    private static EventProtos.Event.Enter formEnterMessage(EnterEventData enterEventData) {
        EventProtos.Event.Enter.Builder enterBuilder = EventProtos.Event.Enter.newBuilder()
                .setMethodName(enterEventData.methodName)
                .setClassName(enterEventData.className)
                .setIsStatic(enterEventData.isStatic);
        if (enterEventData.parameters != null) {
            for (int i = 0; i < enterEventData.parameters.length; i++) {
                enterBuilder.addParameters(objectToVar(enterEventData.parameters[i]));
            }
        }
        return enterBuilder.build();
    }

    private static void writeToFile(EventProtos.Event event) {
        try {
            event.writeDelimitedTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static EventProtos.Event.Var objectToVar(Object o) {
        EventProtos.Event.Var.Builder varBuilder = EventProtos.Event.Var.newBuilder();
        // TODO: https://stackoverflow.com/questions/29570767/switch-over-type-in-java
        if (o instanceof Integer) {
            varBuilder.setI((Integer) o);
        } else if (o instanceof Long) {
            varBuilder.setJ((Long) o);
        } else if (o instanceof Boolean) {
            varBuilder.setZ((Boolean) o);
        } else if (o instanceof Character) {
            varBuilder.setC((Character) o);
        } else if (o instanceof Short) {
            varBuilder.setS((Short) o);
        } else if (o instanceof Byte) {
            varBuilder.setB((Byte) o);
        } else if (o instanceof Float) {
            varBuilder.setF((Float) o);
        } else if (o instanceof Double) {
            varBuilder.setD((Double) o);
        } else { // object
            varBuilder.setObject(
                    EventProtos.Event.Object.newBuilder()
                            .setType(o.getClass().toString())
                            .setValue(o.toString())
                            .build()
            );
        }
        return varBuilder.build();
    }

    static void printDataForHuman() {
        try (InputStream inputStream = new FileInputStream(file)) {
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            while (event != null) {
                System.out.println(event.toString());
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

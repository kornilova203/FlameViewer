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
    private static Logger logger;

    private static final String PROFILER_DIR_PATH = System.getProperty("user.home") + "/.flamegraph-profiler";
    private static final String EVENTS_DIR_NAME = "events";
    private final LinkedBlockingDeque<EventData> queue = new LinkedBlockingDeque<>();
    private final File outDir;
    private final File file;
    private final OutputStream outputStream;
    private int countEventsAdded = 0;
    private int countEventsLogged = 0;
    boolean isDone = false;

    private Logger() {
        logger = this;

        outDir = getDir(EVENTS_DIR_NAME);
        file = createOutFile();
        System.out.println("Output file: " + file);
        OutputStream temp = null;
        try {
            temp = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        outputStream = temp;
    }

    public static Logger getInstance() {
        if (logger == null) {
            logger = new Logger();
            Thread loggerThread = new Thread(logger, "logging thread");
            loggerThread.setDaemon(true);
            loggerThread.start();

            Runtime.getRuntime().addShutdownHook( new WaitingLoggingToFinish("shutdown-hook"));
        }
        return logger;
    }

    @SuppressWarnings("unused")
    public void addToQueue(EventData eventData) {
        countEventsAdded++;
        queue.add(eventData);
    }


    private int getLargestFileNum() {
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

    private File createOutFile() {
        int max = getLargestFileNum();
        return new File(outDir.getAbsolutePath() + "/events" + ++max + ".ser");
    }

    private static File getDir(String dirName) {
        createIfNotExist(PROFILER_DIR_PATH);
        return createIfNotExist(PROFILER_DIR_PATH + "/" + dirName);
    }

    private static File createIfNotExist(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdir();
            } catch (SecurityException se) {
                se.printStackTrace();
            }
        }
        return dir;
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

    private void logEvent(EventData eventData) {
        isDone = false;
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder()
                .setTime(eventData.time)
                .setThreadId(eventData.threadId);
        if (eventData.getClass() == EnterEventData.class) {
            eventBuilder.setEnter(
                    formEnterMessage((EnterEventData) eventData)
            );
        } else if (eventData.getClass() == ExitEventData.class) {
            eventBuilder.setExit(
                    formExitMessage((ExitEventData) eventData)
            );
        } else {
            eventBuilder.setException(
                    formExceptionEventData((ExceptionEventData) eventData)
            );
        }
        writeToFile(eventBuilder.build());
        countEventsLogged++;
        if (queue.size() == 0) {
            isDone = true;
        }
    }

    private static EventProtos.Event.Exception formExceptionEventData(ExceptionEventData eventData) {
        return EventProtos.Event.Exception.newBuilder()
                .setObject(
                        EventProtos.Var.Object.newBuilder()
                                .setType(eventData.throwable.getClass().toString())
                                .setValue(eventData.throwable.getMessage())
                                .build()
                )
                .build();
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

    private void writeToFile(EventProtos.Event event) {
        try {
            event.writeDelimitedTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static EventProtos.Var objectToVar(Object o) {
        EventProtos.Var.Builder varBuilder = EventProtos.Var.newBuilder();
        if (o == null) {
            varBuilder.setObject(
                    EventProtos.Var.Object.newBuilder()
                    .setValue("")
                    .setType("null")
                    .build()
            );
            return varBuilder.build();
        }
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
                    EventProtos.Var.Object.newBuilder()
                            .setType(o.getClass().toString())
                            .setValue(o.toString())
                            .build()
            );
        }
        return varBuilder.build();
    }

    void printDataForHuman() {
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

    void closeOutputStream() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void printStatus() {
        System.out.println("Events added: " + countEventsAdded +
                " Events logged: " + countEventsLogged +
                " Queue size: " + queue.size());
    }
}

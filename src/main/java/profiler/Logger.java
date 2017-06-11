package profiler;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Thread which writes all events from loggingQueue to file
 */
public class Logger implements Runnable {
    private final File file;
    private static File staticFile; // for debugging

    Logger() {
        File outDir = new File("out");
        createDirIfNotExist(outDir);

        file = createOutFile(outDir);
        staticFile = file;
        System.out.println(file.getAbsolutePath());
    }

    private File createOutFile(File outDir) {
        File[] files = outDir.listFiles();
        int max = 0;
        if (files != null) {
            Optional<String> optionalMaxFileName = Arrays.stream(files)
                    .map(File::getName) // get names of files
                    .max(Comparator.naturalOrder());

            if (optionalMaxFileName.isPresent()) {
                Pattern getNumPattern = Pattern.compile("[0-9]+");
                Matcher m = getNumPattern.matcher(optionalMaxFileName.get());
                if (m.find()) {
                    max = Integer.parseInt(m.group());
                }
            }
        }
        return new File(outDir.getAbsolutePath() + "/events" + ++max + ".ser");
    }

    private void createDirIfNotExist(File outDir) {
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
            if (LoggingQueue.isEmpty()) {
                Thread.yield();
            } else {
                logEvent(LoggingQueue.dequeue());
            }
        }
    }

    private void logEvent(EventData eventData) {
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder()
                .setTime(eventData.time)
                .setThreadId(eventData.threadId);
        if (eventData.getClass() == EnterEventData.class) {
            EnterEventData enterEventData = (EnterEventData) eventData;
            EventProtos.Event.Enter.Builder enterBuilder = EventProtos.Event.Enter.newBuilder()
                    .setMethodName(enterEventData.methodName)
                    .setClassName(enterEventData.className)
                    .setIsStatic(enterEventData.isStatic);
            if (enterEventData.parameters != null) {
                List<EventProtos.Event.Var> parameters = Arrays.stream(enterEventData.parameters)
                        .map(this::objectToVar)
                        .collect(Collectors.toList());
                enterBuilder.addAllParameters(parameters);
            }
            eventBuilder.setEnter(enterBuilder.build());
        } else {
            ExitEventData exitEventData = (ExitEventData) eventData;
            EventProtos.Event.Exit.Builder exitBuilder = EventProtos.Event.Exit.newBuilder();
            if (exitEventData.returnValue != null) {
                exitBuilder.setReturnValue(objectToVar(exitEventData.returnValue));
            }
            eventBuilder.setExit(exitBuilder.build());
        }
        writeToFile(eventBuilder.build());
    }

    private void writeToFile(EventProtos.Event event) {
        try (OutputStream outputStream = new FileOutputStream(file, true)) {
            event.writeDelimitedTo(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private EventProtos.Event.Var objectToVar(Object o) {
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
            varBuilder.setA(o.toString());
        }
        return varBuilder.build();
    }

    static void printDataForHuman() {
        try {
            InputStream inputStream = new FileInputStream(staticFile);
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

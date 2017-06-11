package profiler;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Logger implements Runnable {
    private File file = new File("out/events.ser");

    @Override
    public void run() {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            if (Agent.loggingQueue.isEmpty()) {
                Thread.yield();
            } else {
                logEvent(Agent.loggingQueue.dequeue());
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
}

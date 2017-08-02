package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.List;

abstract class MethodEventData implements EventData {

    Long getThreadNameId(List<EventProtos.Event> events) {
        Long threadNameId = LoggerQueue.registeredThreadNames.get(threadName);
        if (threadNameId == null) {
            threadNameId = ++LoggerQueue.threadNamesId;
            LoggerQueue.registeredThreadNames.put(threadName, threadNameId);
            events.add(createNewThreadEvent(threadNameId, threadName));
        }
        return threadNameId;
    }

    private EventProtos.Event createNewThreadEvent(Long id, String name) {
        return EventProtos.Event.newBuilder()
                .setNewThread(EventProtos.Event.Map.newBuilder()
                        .setId(id)
                        .setName(name)
                        .build()
                ).build();
    }

    MethodEventData(long time, String threadName) {
        this.time = time;
        this.threadName = threadName;
    }

    final long time;
    final String threadName;

    EventProtos.Var objectToVar(Object o) {
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
            addObject(varBuilder, o);

        }
        return varBuilder.build();
    }

    private static void addObject(EventProtos.Var.Builder varBuilder, Object o) {
        EventProtos.Var.Object.Builder objectBuilder = EventProtos.Var.Object.newBuilder();
        objectBuilder.setType(o.getClass().toString());
        try {
            objectBuilder.setValue(o.toString());
        } catch (Throwable throwable) {
            objectBuilder.setValue("");
        }
        varBuilder.setObject(objectBuilder.build());
    }
}

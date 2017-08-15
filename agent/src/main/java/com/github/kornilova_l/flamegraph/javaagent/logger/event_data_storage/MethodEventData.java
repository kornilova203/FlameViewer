package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.ClassNamesMap;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.NamesMap;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.ThreadNamesMap;
import com.github.kornilova_l.flamegraph.proto.EventProtos;
import com.github.kornilova_l.flamegraph.proto.EventProtos.Event;

import java.util.LinkedList;
import java.util.List;

abstract public class MethodEventData {
    private String threadName;
    private String className;
    private long startTime;
    private long duration;
    private String methodName;
    private String desc;
    private boolean isStatic;
    private Object[] parameters;
    private static ClassNamesMap classNamesMap = new ClassNamesMap();
    private static ThreadNamesMap threadNamesMap = new ThreadNamesMap();

    MethodEventData(String threadName,
                    String className,
                    long startTime,
                    long duration,
                    String methodName,
                    String desc,
                    boolean isStatic,
                    Object[] parameters) {
        this.threadName = threadName;
        this.className = className;
        this.startTime = startTime;
        this.duration = duration;
        this.methodName = methodName;
        this.desc = desc;
        this.isStatic = isStatic;
        this.parameters = parameters;
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

    public List<Event> getEvents() {
        List<Event> events = new LinkedList<>();
        Event.Builder eventBuilder = Event.newBuilder();
        Event.MethodEvent.Builder methodEventBuilder = Event.MethodEvent.newBuilder();
        setCommonInfo(methodEventBuilder, events);

        setResult(methodEventBuilder);

        eventBuilder.setMethodEvent(methodEventBuilder);
        events.add(eventBuilder.build());

        return events;
    }

    abstract void setResult(Event.MethodEvent.Builder methodEventBuilder);

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

    private void setCommonInfo(Event.MethodEvent.Builder methodEventBuilder, List<Event> events) {
        long classNameId = getClassNameId(events);
        long threadNameId = getThreadNameId(events);

        methodEventBuilder.setStartTime(startTime)
                .setDuration(duration)
                .setDesc(desc)
                .setThreadId(threadNameId)
                .setClassNameId(classNameId)
                .setMethodName(methodName)
                .setIsStatic(isStatic);

        setParameters(methodEventBuilder);

    }

    private long getThreadNameId(List<Event> events) {
        return getIdAndRegister(events, threadNamesMap, threadName);
    }

    private static long getIdAndRegister(List<Event> events,
                                         NamesMap namesMap,
                                         String name) {
        boolean isRegistered = namesMap.isRegistered(name);
        long id = namesMap.getId(name);
        if (!isRegistered) {
            events.add(namesMap.getRegistrationEvent(id, name));
        }
        return id;
    }

    private long getClassNameId(List<Event> events) {
        return getIdAndRegister(events, classNamesMap, className);
    }

    private void setParameters(Event.MethodEvent.Builder methodEventBuilder) {
        if (parameters != null) {
            for (Object parameter : parameters) {
                methodEventBuilder.addParameters(objectToVar(parameter));
            }
        }
    }
}

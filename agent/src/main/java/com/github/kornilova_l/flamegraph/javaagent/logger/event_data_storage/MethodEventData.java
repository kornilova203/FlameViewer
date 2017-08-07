package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proto.EventProtos;

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

    abstract public List<EventProtos.Event> getEvents();

    Long getClassNameId(List<EventProtos.Event> events) {
        Long classNameId = LoggerQueue.registeredClassNames.get(className);
        if (classNameId == null) {
            classNameId = ++LoggerQueue.classNamesId;
            LoggerQueue.registeredClassNames.put(className, classNameId);
            events.add(createNewClassEvent(classNameId, className));
        }
        return classNameId;
    }

    private EventProtos.Event createNewClassEvent(Long id, String name) {
        return EventProtos.Event.newBuilder()
                .setNewClass(EventProtos.Event.Map.newBuilder()
                        .setId(id)
                        .setName(name)
                        .build()
                ).build();
    }

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

    void setCommonInfo(EventProtos.Event.MethodEvent.Builder methodEventBuilder, List<EventProtos.Event> events) {
        Long classNameId = getClassNameId(events);
        Long threadNameId = getThreadNameId(events);

        methodEventBuilder.setStartTime(startTime)
                .setDuration(duration)
                .setDesc(desc)
                .setThreadId(threadNameId)
                .setClassNameId(classNameId)
                .setMethodName(methodName)
                .setIsStatic(isStatic);

        setParameters(methodEventBuilder);

    }

    void setParameters(EventProtos.Event.MethodEvent.Builder methodEventBuilder) {
        if (parameters != null) {
            for (Object parameter : parameters) {
                methodEventBuilder.addParameters(objectToVar(parameter));
            }
        }
    }
}

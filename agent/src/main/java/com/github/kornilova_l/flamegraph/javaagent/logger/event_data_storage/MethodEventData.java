package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.ClassNamesMap;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.NamesMap;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps.ThreadNamesMap;
import com.github.kornilova_l.flamegraph.proto.EventProtos.Event;
import com.github.kornilova_l.flamegraph.proto.EventProtos.Parameter;
import com.github.kornilova_l.flamegraph.proto.EventProtos.Var;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

abstract public class MethodEventData {
    private final String threadName;
    private final String className;
    private final long startTime;
    private final long duration;
    private final String methodName;
    private final String desc;
    private final String savedParameters;
    private List<Integer> savedParametersList;
    private final boolean isStatic;
    private final Object[] parameters;
    private final static ClassNamesMap classNamesMap = new ClassNamesMap();
    private final static ThreadNamesMap threadNamesMap = new ThreadNamesMap();

    MethodEventData(String threadName,
                    String className,
                    long startTime,
                    long duration,
                    String methodName,
                    String desc,
                    boolean isStatic,
                    Object[] parameters,
                    String savedParameters) {
        this.threadName = threadName;
        this.className = className;
        this.startTime = startTime;
        this.duration = duration;
        this.methodName = methodName;
        this.desc = desc;
        this.isStatic = isStatic;
        this.parameters = parameters;
        this.savedParameters = savedParameters;
    }

    private static void addObject(Var.Builder varBuilder, Object o) {
        Var.Object.Builder objectBuilder = Var.Object.newBuilder();
        objectBuilder.setType(o.getClass().toString());
        try {
            objectBuilder.setValue(o.toString());
        } catch (Throwable throwable) {
            objectBuilder.setValue("");
        }
        varBuilder.setObject(objectBuilder.build());
    }

    public List<Event> getEvents() {
        initSaveParametersList();
        List<Event> events = new LinkedList<>();
        Event.Builder eventBuilder = Event.newBuilder();
        Event.MethodEvent.Builder methodEventBuilder = Event.MethodEvent.newBuilder();
        setCommonInfo(methodEventBuilder, events);

        setResult(methodEventBuilder);

        eventBuilder.setMethodEvent(methodEventBuilder);
        events.add(eventBuilder.build());

        return events;
    }

    private void initSaveParametersList() {
        savedParametersList = new ArrayList<>();
        if (Objects.equals(savedParameters, "")) {
            return;
        }
        String[] indexes = savedParameters.split(",");
        for (String index : indexes) {
            savedParametersList.add(Integer.parseInt(index));
        }
    }

    abstract void setResult(Event.MethodEvent.Builder methodEventBuilder);

    Var objectToVar(Object o) {
        Var.Builder varBuilder = Var.newBuilder();
        if (o == null) {
            varBuilder.setObject(
                    Var.Object.newBuilder()
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
            for (int i = 0; i < parameters.length; i++) {
                methodEventBuilder.addParameters(createParameter(objectToVar(parameters[i]), i));
            }
        }
    }

    private Parameter createParameter(Var var, int i) {
        return Parameter.newBuilder()
                .setVar(var)
                .setIndex(savedParametersList.get(i))
                .build();
    }
}

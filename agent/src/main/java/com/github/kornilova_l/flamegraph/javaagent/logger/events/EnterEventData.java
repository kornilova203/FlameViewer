package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.LinkedList;
import java.util.List;

public class EnterEventData extends MethodEventData {
    private final String className;
    private String methodName;
    private String description;
    private boolean isStatic;
    private Object[] parameters;

    public EnterEventData(String threadName,
                          long startTime,
                          String className,
                          String methodName,
                          String description,
                          boolean isStatic,
                          Object[] parameters) {
        super(startTime, threadName);
        this.className = className;
        this.methodName = methodName;
        this.description = description;
        this.isStatic = isStatic;
        this.parameters = parameters;
    }

    public List<EventProtos.Event> getEvents() {
        List<EventProtos.Event> events = new LinkedList<>();
        Long classNameId = getClassNameId(events);
        Long threadNameId = getThreadNameId(events);

        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadNameId);

        methodEventBuilder.setEnter(
                formEnterMessage(classNameId)
        );
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        events.add(eventBuilder.build());
        return events;
    }

    private Long getClassNameId(List<EventProtos.Event> events) {
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

    private EventProtos.Event.MethodEvent.Enter formEnterMessage(Long classNameId) {
        EventProtos.Event.MethodEvent.Enter.Builder enterBuilder = EventProtos.Event.MethodEvent.Enter.newBuilder()
                .setMethodName(methodName)
                .setClassNameId(classNameId)
                .setDescription(description)
                .setIsStatic(isStatic);
        if (parameters != null) {
            for (Object parameter : parameters) {
                enterBuilder.addParameters(objectToVar(parameter));
            }
        }
        return enterBuilder.build();
    }
}

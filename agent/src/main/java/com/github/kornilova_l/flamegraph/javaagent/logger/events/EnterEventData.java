package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class EnterEventData extends MethodEventData {
    private final long classNameId;
    private String methodName;
    private String description;
    private boolean isStatic;
    private Object[] parameters;

    public EnterEventData(long threadId,
                          long startTime,
                          long classNameId,
                          String methodName,
                          String description,
                          boolean isStatic,
                          Object[] parameters) {
        super(startTime, threadId);
        this.classNameId = classNameId;
        this.methodName = methodName;
        this.description = description;
        this.isStatic = isStatic;
        this.parameters = parameters;
    }

    public EventProtos.Event getEventProto() {
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadId);

        methodEventBuilder.setEnter(
                formEnterMessage()
        );
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        return eventBuilder.build();
    }

    private EventProtos.Event.MethodEvent.Enter formEnterMessage() {
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

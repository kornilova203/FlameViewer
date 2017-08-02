package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ExitEventData extends MethodEventData {
    private Object returnValue;

    public ExitEventData(Object returnValue, long threadId, long exitTime) {
        super(exitTime, threadId);
        this.returnValue = returnValue;
    }

    @Override
    public EventProtos.Event getEventProto() {
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadId);
        methodEventBuilder.setExit(
                formExitMessage()
        );
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        return eventBuilder.build();
    }

    private EventProtos.Event.MethodEvent.Exit formExitMessage() {
        EventProtos.Event.MethodEvent.Exit.Builder exitBuilder = EventProtos.Event.MethodEvent.Exit.newBuilder();
        if (returnValue != null) {
            exitBuilder.setReturnValue(objectToVar(returnValue));
        }
        return exitBuilder.build();
    }
}

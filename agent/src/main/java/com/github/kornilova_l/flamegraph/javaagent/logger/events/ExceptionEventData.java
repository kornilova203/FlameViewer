package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ExceptionEventData extends MethodEventData {
    private Throwable throwable;

    public ExceptionEventData(Throwable throwable, long threadId, long exitTime) {
        super(exitTime, threadId);
        this.throwable = throwable;
    }

    @Override
    public EventProtos.Event getEventProto() {
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadId);
        methodEventBuilder.setException(
                formExceptionEventData());
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        return eventBuilder.build();
    }

    private EventProtos.Event.MethodEvent.Exception formExceptionEventData() {
        if (throwable == null) {
            return EventProtos.Event.MethodEvent.Exception.newBuilder()
                    .setObject(EventProtos.Var.Object.newBuilder()
                            .setValue("")
                            .setType("")
                            .build()).build();
        }
        return EventProtos.Event.MethodEvent.Exception.newBuilder()
                .setObject(
                        EventProtos.Var.Object.newBuilder()
                                .setType(throwable.getClass().toString())
                                .setValue(throwable.getMessage())
                                .build()
                )
                .build();
    }
}

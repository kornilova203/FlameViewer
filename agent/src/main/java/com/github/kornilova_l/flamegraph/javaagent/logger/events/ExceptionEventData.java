package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.LinkedList;
import java.util.List;

public class ExceptionEventData extends MethodEventData {
    private Throwable throwable;

    public ExceptionEventData(Throwable throwable, String threadName, long exitTime) {
        super(exitTime, threadName);
        this.throwable = throwable;
    }

    @Override
    public List<EventProtos.Event> getEvents() {
        List<EventProtos.Event> events = new LinkedList<>();
        Long threadNameId = getThreadNameId(events);

        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadNameId);
        methodEventBuilder.setException(
                formExceptionEventData());
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        events.add(eventBuilder.build());
        return events;
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

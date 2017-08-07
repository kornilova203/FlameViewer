package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.LinkedList;
import java.util.List;

public class ThrowableEventData extends MethodEventData {
    private Throwable throwable;

    public ThrowableEventData(Thread thread,
                              String className,
                              long startTime,
                              long duration,
                              String methodName,
                              String desc,
                              boolean isStatic,
                              Object[] parameters,
                              Throwable throwable) {
        super(thread.getName(), className, startTime, duration, methodName, desc, isStatic, parameters);
        this.throwable = throwable;
    }

    @Override
    public List<EventProtos.Event> getEvents() {
        List<EventProtos.Event> events = new LinkedList<>();
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder();
        setCommonInfo(methodEventBuilder, events);

        setThrowable(methodEventBuilder);

        eventBuilder.setMethodEvent(methodEventBuilder);
        events.add(eventBuilder.build());

        return events;
    }

    private void setThrowable(EventProtos.Event.MethodEvent.Builder methodEventBuilder) {
        if (throwable == null) {
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder().build()
            );
        } else {
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder()
                            .setType(throwable.getClass().toString())
                            .setValue(throwable.getMessage()));
        }
    }
}

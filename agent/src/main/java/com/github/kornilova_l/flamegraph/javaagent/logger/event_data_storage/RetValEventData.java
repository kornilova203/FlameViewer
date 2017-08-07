package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.LinkedList;
import java.util.List;

public class RetValEventData extends MethodEventData {
    public RetValEventData(Thread thread,
                           String className,
                           long startTime,
                           long duration,
                           String methodName,
                           String desc,
                           boolean isStatic,
                           Object[] parameters,
                           Object retVal) {
        super(thread.getName(), className, startTime, duration, methodName, desc, isStatic, parameters);
        this.retVal = retVal;
    }

    private Object retVal;

    @Override
    public List<EventProtos.Event> getEvents() {
        List<EventProtos.Event> events = new LinkedList<>();
        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder();
        setCommonInfo(methodEventBuilder, events);

        methodEventBuilder.setReturnValue(objectToVar(retVal));

        eventBuilder.setMethodEvent(methodEventBuilder);
        events.add(eventBuilder.build());

        return events;
    }
}

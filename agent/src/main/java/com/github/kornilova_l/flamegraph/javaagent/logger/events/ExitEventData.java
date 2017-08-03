package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.LinkedList;
import java.util.List;

public class ExitEventData extends EventData {
    private Object returnValue;

    public ExitEventData(Object returnValue, String threadName, long exitTime) {
        super(exitTime, threadName);
        this.returnValue = returnValue;
    }

    @Override
    public List<EventProtos.Event> getEvents() {
        List<EventProtos.Event> events = new LinkedList<>();
        Long threadNameId = getThreadNameId(events);

        EventProtos.Event.Builder eventBuilder = EventProtos.Event.newBuilder();
        EventProtos.Event.MethodEvent.Builder methodEventBuilder = EventProtos.Event.MethodEvent.newBuilder()
                .setTime(time)
                .setThreadId(threadNameId);
        methodEventBuilder.setExit(
                formExitMessage()
        );
        eventBuilder.setMethodEvent(methodEventBuilder.build());
        events.add(eventBuilder.build());
        return events;
    }

    private EventProtos.Event.MethodEvent.Exit formExitMessage() {
        EventProtos.Event.MethodEvent.Exit.Builder exitBuilder = EventProtos.Event.MethodEvent.Exit.newBuilder();
        if (returnValue != null) {
            exitBuilder.setReturnValue(objectToVar(returnValue));
        }
        return exitBuilder.build();
    }
}

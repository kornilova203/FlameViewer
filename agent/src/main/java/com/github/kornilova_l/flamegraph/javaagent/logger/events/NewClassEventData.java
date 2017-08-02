package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class NewClassEventData implements EventData {
    private final long id;
    private final String name;

    public NewClassEventData(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public EventProtos.Event getEventProto() {
        return EventProtos.Event.newBuilder()
                .setNewClass(EventProtos.Event.Map.newBuilder()
                        .setId(id)
                        .setName(name)
                        .build()
                ).build();
    }
}

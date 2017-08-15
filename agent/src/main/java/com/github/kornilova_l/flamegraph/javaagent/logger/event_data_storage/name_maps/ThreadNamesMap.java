package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ThreadNamesMap extends NamesMap {
    @Override
    public EventProtos.Event getRegistrationEvent(Long id, String name) {
        return EventProtos.Event.newBuilder()
                .setNewThread(EventProtos.Event.Map.newBuilder()
                        .setId(id)
                        .setName(name)
                ).build();
    }
}

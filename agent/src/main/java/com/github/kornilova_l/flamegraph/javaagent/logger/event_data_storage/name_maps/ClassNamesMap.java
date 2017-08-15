package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ClassNamesMap extends NamesMap {
    @Override
    public EventProtos.Event getRegistrationEvent(Long id, String name) {
        return EventProtos.Event.newBuilder()
                .setNewClass(EventProtos.Event.Map.newBuilder()
                        .setId(id)
                        .setName(name)
                ).build();
    }
}

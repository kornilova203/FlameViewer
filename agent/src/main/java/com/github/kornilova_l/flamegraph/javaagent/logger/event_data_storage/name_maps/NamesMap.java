package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.name_maps;

import com.github.kornilova_l.flamegraph.proto.EventProtos.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds maps for thread names and class names
 */
public abstract class NamesMap {
    private Map<String, Long> map = new HashMap<>();
    private long size = 0;

    public boolean isRegistered(String name) {
        return map.containsKey(name);
    }

    public long getId(String name) {
        return map.computeIfAbsent(name, k -> ++size);
    }

    public abstract Event getRegistrationEvent(Long id, String name);
}

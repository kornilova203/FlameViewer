package com.github.kornilova_l.flamegraph.javaagent.logger.events;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.util.List;

public interface EventData {
    List<EventProtos.Event> getEvents();
}

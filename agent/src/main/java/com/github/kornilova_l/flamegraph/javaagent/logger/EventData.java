package com.github.kornilova_l.flamegraph.javaagent.logger;

abstract class EventData {
    EventData(long time, long threadId) {
        this.time = time;
        this.threadId = threadId;
    }

    long time;
    long threadId;
}

package com.github.kornilova_l.profiler.logger;

abstract class EventData {
    EventData(long time, long threadId) {
        this.time = time;
        this.threadId = threadId;
    }

    long time;
    long threadId;
}

package com.github.kornilova_l.profiler.logger;

public class ExitEventData extends EventData {
    Object returnValue;
    public ExitEventData(Object returnValue, long threadId, long exitTime) {
        this.threadId = threadId;
        this.time = exitTime;
        this.returnValue = returnValue;
    }
}

package com.github.kornilova_l.profiler.logger;

public class ExitEventData extends EventData {
    Object returnValue;
    public ExitEventData(Object returnValue, long threadId, long exitTime) {
        super(exitTime, threadId);
        this.returnValue = returnValue;
    }
}

package com.github.kornilova_l.profiler.logger;

public class ExceptionEventData extends EventData {
    Throwable throwable;
    public ExceptionEventData(Throwable throwable, long threadId, long exitTime) {
        super(exitTime, threadId);
        this.throwable = throwable;
    }
}

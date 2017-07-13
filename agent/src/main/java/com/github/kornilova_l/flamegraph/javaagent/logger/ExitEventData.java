package com.github.kornilova_l.flamegraph.javaagent.logger;

public class ExitEventData extends EventData {
    Object returnValue;
    public ExitEventData(Object returnValue, long threadId, long exitTime) {
        super(exitTime, threadId);
        this.returnValue = returnValue;
    }
}

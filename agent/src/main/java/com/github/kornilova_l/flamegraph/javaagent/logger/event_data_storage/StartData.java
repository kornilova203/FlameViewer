package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

public class StartData {
    private final long startTime;
    private final Object[] parameters;
    private long duration;

    public StartData(long startTime, Object[] parameters) {
        this.startTime = startTime;
        this.parameters = parameters;
    }

    public void setDuration(long endTime) {
        duration = endTime - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public long getDuration() {
        return duration;
    }
}

package com.github.kornilova_l.flamegraph.proxy;

public class StartData {
    private final long startTime;
    private final Object[] parameters;
    private long duration;
    private boolean thrownByMethod = false;

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

    public void setThrownByMethod() {
        thrownByMethod = true;
    }

    public boolean isThrownByMethod() {
        return thrownByMethod;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public long getDuration() {
        return duration;
    }
}

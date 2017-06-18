package com.github.kornilova_l.profiler;

public class EnterEventData extends EventData {
    String className;
    String methodName;
    boolean isStatic;
    Object[] parameters;
    public EnterEventData(long threadId,
                   long startTime,
                   String className,
                   String methodName,
                   boolean isStatic,
                   Object[] parameters) {
        this.threadId = threadId;
        this.time = startTime;
        this.className = className;
        this.methodName = methodName;
        this.isStatic = isStatic;
        this.parameters = parameters;
    }
}

package com.github.kornilova_l.profiler.logger;

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
        super(startTime, threadId);
        this.className = className;
        this.methodName = methodName;
        this.isStatic = isStatic;
        this.parameters = parameters;
    }
}

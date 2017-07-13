package com.github.kornilova_l.flamegraph.javaagent.logger;

public class EnterEventData extends EventData {
    String className;
    String methodName;
    String description;
    boolean isStatic;
    Object[] parameters;
    public EnterEventData(long threadId,
                   long startTime,
                   String className,
                   String methodName,
                   String description,
                   boolean isStatic,
                   Object[] parameters) {
        super(startTime, threadId);
        this.className = className;
        this.methodName = methodName;
        this.description = description;
        this.isStatic = isStatic;
        this.parameters = parameters;
    }
}

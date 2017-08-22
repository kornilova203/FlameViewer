package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class RetValEventData extends MethodEventData {
    private Object retVal;

    public RetValEventData(Thread thread,
                           String className,
                           long startTime,
                           long duration,
                           String methodName,
                           String desc,
                           boolean isStatic,
                           Object[] parameters,
                           Object retVal,
                           String savedParameters) {
        super(thread.getName(), className, startTime, duration, methodName, desc, isStatic, parameters, savedParameters);
        this.retVal = retVal;
    }

    @Override
    void setResult(EventProtos.Event.MethodEvent.Builder methodEventBuilder) {
        if (retVal != null) {
            methodEventBuilder.setReturnValue(objectToVar(retVal));
        } else {
            methodEventBuilder.setReturnValue(EventProtos.Var.newBuilder());
        }
    }
}

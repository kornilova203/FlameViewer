package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ThrowableEventData extends MethodEventData {
    private Throwable throwable;

    public ThrowableEventData(Thread thread,
                              String className,
                              long startTime,
                              long duration,
                              String methodName,
                              String desc,
                              boolean isStatic,
                              Object[] parameters,
                              Throwable throwable,
                              String savedParameters) {
        super(thread.getName(), className, startTime, duration, methodName, desc, isStatic, parameters, savedParameters);
        this.throwable = throwable;
    }

    @Override
    void setResult(EventProtos.Event.MethodEvent.Builder methodEventBuilder) {
        if (throwable == null) {
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder().build()
            );
        } else {
            String message = throwable.getMessage();
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder()
                            .setType(throwable.getClass().toString())
                            .setValue(message == null ? "" : message));
        }
    }
}

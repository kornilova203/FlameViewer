package com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage;

import com.github.kornilova_l.flamegraph.proto.EventProtos;

public class ThrowableEventData extends MethodEventData {
    private final Throwable throwable;
    private final boolean saveMessage;

    public ThrowableEventData(Thread thread,
                              String className,
                              long startTime,
                              long duration,
                              String methodName,
                              String desc,
                              boolean isStatic,
                              Object[] parameters,
                              Throwable throwable,
                              boolean saveMessage,
                              String savedParameters) {
        super(thread.getName(), className, startTime, duration, methodName, desc, isStatic, parameters, savedParameters);
        this.throwable = throwable;
        this.saveMessage = saveMessage;
    }

    @Override
    void setResult(EventProtos.Event.MethodEvent.Builder methodEventBuilder) {
        if (throwable == null) { // this should not happen but who knows
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder().build()
            );
        } else {
            String message = null;
            if (saveMessage) {
                message = throwable.getMessage();
            }
            methodEventBuilder.setThrowable(
                    EventProtos.Var.Object.newBuilder()
                            .setType(throwable.getClass().toString())
                            .setValue(message == null ? "" : message));
        }
    }
}

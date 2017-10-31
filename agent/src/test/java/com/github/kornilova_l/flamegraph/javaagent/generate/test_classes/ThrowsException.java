package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

public class ThrowsException {
    public static void main(String[] args) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
//        try {
//            startData.setDuration(System.currentTimeMillis());
//            if (startData.getDuration() > 1) {
//                startData.setThrownByMethod();
//                LoggerQueue.addToQueue(null,
//                        false,
//                        startData.getStartTime(),
//                        startData.getDuration(),
//                        startData.getParameters(),
//                        Thread.currentThread(),
//                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/ThrowsException",
//                        "main",
//                        true,
//                        "([Ljava/lang/String;)V",
//                        ""
//                );
//            }
        throw new AssertionError("error");
//        } catch (Throwable throwable) {
//            if (!startData.isThrownByMethod()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData.getStartTime(),
//                            startData.getDuration(),
//                            startData.getParameters(),
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/ThrowsException",
//                            "main",
//                            "([Ljava/lang/String;)V",
//                            true,
//                            ""
//                    );
//                }
//            }
//            throw throwable;
//        }
    }
}

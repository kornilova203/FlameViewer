package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

public class HasCatch {
    public static void main(String[] args) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
//        try {
        try {
//                startData.setThrownByMethod();
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData.getStartTime(),
//                            startData.getDuration(),
//                            startData.getParameters(),
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
//                            "main",
//                            true,
//                            "([Ljava/lang/String;)V");
//                }
            throw new AssertionError("");
        } catch (Error throwable) {
            System.out.println("Normal");
        }
//        } catch (Throwable throwable) {
//            if (!startData.isThrownByMethod()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData.getStartTime(),
//                            startData.getDuration(),
//                            startData.getParameters(),
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
//                            "main",
//                            true,
//                            "([Ljava/lang/String;)V");
//                }
//            }
//            throw throwable;
//        }
    }
}

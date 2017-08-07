package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;


public class OneMethod {
    public static int main(String[] args) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod",
//                    "main",
//                    "([Ljava/lang/String;)I",
//                    true);
//        }
        return 23;
    }
}

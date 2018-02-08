package com.github.kornilova_l.proxy_test_classes;

public class ThrowsException implements TestModule {
    @Override
    public int run(long l, String s, double d) {
        System.out.println(l + s + d);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("I am an exception");
    }
}

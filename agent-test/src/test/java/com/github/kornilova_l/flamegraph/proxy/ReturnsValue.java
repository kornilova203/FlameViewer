package com.github.kornilova_l.flamegraph.proxy;

public class ReturnsValue implements TestModule {

    @Override
    public int run(long l, String s, double d) {
        System.out.println(l + s + d);
        return 42;
    }
}

package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

public class HasIf {
    public static int main(int val) {
        int res = 0;
        if (val > 0) {
            res++;
        }
        return res;
    }
}

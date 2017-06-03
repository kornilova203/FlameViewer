package org.jetbrains.test;


import Profiler.State;

import static Profiler.Profiler.methodStart;

public class SimpleExampleWithProfiler {
    public static void main(String[] args) {
        State state = methodStart();
        state.methodFinish();
    }
}

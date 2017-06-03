package org.jetbrains.test;

import profiler.State;
import static profiler.Profiler.methodStart;

public class SimpleExampleWithProfiler {
    public void start() {
        State state = methodStart();
        state.methodFinish();
    }
}

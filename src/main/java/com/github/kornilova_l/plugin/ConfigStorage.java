package com.github.kornilova_l.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import java.util.HashSet;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.State> {
    public static class State {
        public State() {
            test = 23;
            includedMethods = new HashSet<>();
            excludedMethods = new HashSet<>();
        }
        public int test;
        public HashSet<String> includedMethods;
        public HashSet<String> excludedMethods;
    }

    State state;

    public ConfigStorage() {
        state = new State();
    }

    public ConfigStorage.State getState() {
        return state;
    }

    public void loadState(ConfigStorage.State state) {
        this.state = state;
    }
}

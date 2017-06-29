package com.github.kornilova_l.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import java.util.HashMap;
import java.util.Map;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.State> {
    public static class State {
        public State() {
            this(
                    new HashMap<>(),
                    new HashMap<>()
            );
        }

        public State(Map<Integer, String> includedMethodsMap,
                     Map<Integer, String> excludedMethodsMap) {

            this.includedMethodsMap = includedMethodsMap;
            this.excludedMethodsMap = excludedMethodsMap;
        }

        /*
        map configuration hashCode to profiler config config
         */
        public Map<Integer, String> includedMethodsMap;
        public Map<Integer, String> excludedMethodsMap;
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

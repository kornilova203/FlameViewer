package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import java.util.HashMap;
import java.util.Map;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.State> {
    public static class State {
        public State() {
            this(new HashMap<>());
        }

        public State(Map<String, Map<String, String>> configs) {
            this.configs = configs;
        }

        // <name_of_config> -> (included)|(excluded) -> config_string
        @SuppressWarnings("PublicField")
        public Map<String, Map<String, String>> configs;
    }

    private State state;

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

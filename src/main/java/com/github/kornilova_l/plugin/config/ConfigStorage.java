package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import java.util.*;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.State> {
    @SuppressWarnings("PublicField")
    public static class State {
        public State() {
            this(new LinkedList<>());
        }

        private State(Collection<Config> profilerSettings) {
            this.profilerSettings = profilerSettings;
        }

        public Collection<Config> profilerSettings;

        public List<String> getNamesList() {
            LinkedList<String> list = new LinkedList<>();
            if (profilerSettings.size() == 0) {
                return list;
            }
            for (Config ps : profilerSettings) {
                list.add(ps.name);
            }
            return list;
        }

        public Config getSetting(String configName) {
            for (Config ps : profilerSettings) {
                if (Objects.equals(ps.name, configName)) {
                    return ps;
                }
            }
            return null;
        }
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

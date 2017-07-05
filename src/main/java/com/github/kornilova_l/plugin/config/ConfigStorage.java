package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

import java.util.*;

@State(name = "flamegraph-profiler")
public class ConfigStorage implements PersistentStateComponent<ConfigStorage.Config> {
    @SuppressWarnings("PublicField")
    public static class Config {

        public Config() {
            this(new ConfigNode(), new HashSet<>());
        }

        private Config(ConfigNode baseNode, Collection<String> patterns) {
            this.baseNode = baseNode;
            this.patterns = patterns;
        }

        public ConfigNode baseNode; // node for tree of methods
        public Collection<String> patterns;
    }

    Config config;

    public ConfigStorage() {
        config = new Config();
    }

    public Config getState() {
        return config;
    }

    public void loadState(Config config) {
        this.config = config;
    }
}

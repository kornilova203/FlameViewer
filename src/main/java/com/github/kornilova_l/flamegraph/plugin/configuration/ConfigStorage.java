package com.github.kornilova_l.flamegraph.plugin.configuration;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;

@State(name = "flamegraph-profiler")
class ConfigStorage implements PersistentStateComponent<Configuration> {
    Configuration configuration;

    public ConfigStorage() {
        configuration = new Configuration();
    }

    public Configuration getState() {
        return configuration;
    }

    public void loadState(Configuration config) {
        this.configuration = config;
    }
}

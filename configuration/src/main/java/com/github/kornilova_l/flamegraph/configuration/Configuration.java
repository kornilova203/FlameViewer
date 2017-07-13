package com.github.kornilova_l.flamegraph.configuration;

import java.util.Collection;
import java.util.TreeSet;

public class Configuration {
    private Collection<MethodConfig> includingMethodConfigs; // node for tree of includingMethodConfigs
    private Collection<MethodConfig> excludingMethodConfigs; // node for tree of includingMethodConfigs

    public Configuration() {
        this(new TreeSet<>(), new TreeSet<>());
    }

    private Configuration(TreeSet<MethodConfig> includingMethodConfigs, TreeSet<MethodConfig> excludingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
        this.excludingMethodConfigs = excludingMethodConfigs;
    }

    public Collection<MethodConfig> getIncludingMethodConfigs() {
        return includingMethodConfigs;
    }

    public void setIncludingMethodConfigs(Collection<MethodConfig> includingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
    }

    public Collection<MethodConfig> getExcludingMethodConfigs() {
        return excludingMethodConfigs;
    }

    public void setExcludingMethodConfigs(Collection<MethodConfig> excludingMethodConfigs) {
        this.excludingMethodConfigs = excludingMethodConfigs;
    }
}

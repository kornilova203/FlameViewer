package com.github.kornilova_l.flamegraph.configuration;

import java.util.Collection;
import java.util.TreeSet;

public class Configuration {
    private Collection<MethodConfig> includingMethodConfigs;
    private Collection<MethodConfig> excludingMethodConfigs;

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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (MethodConfig methodConfig : includingMethodConfigs) {
            if (methodConfig.isEnabled()) {
                stringBuilder.append(methodConfig.toString()).append("\n");
            }
        }
        for (MethodConfig methodConfig : excludingMethodConfigs) {
            if (methodConfig.isEnabled()) {
                stringBuilder.append("!").append(methodConfig.toString()).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public void addMethodConfig(MethodConfig methodConfig,
                                boolean isExcluded) {
        if (isExcluded) {
            excludingMethodConfigs.add(methodConfig);
        } else {
            includingMethodConfigs.add(methodConfig);
        }
    }
}

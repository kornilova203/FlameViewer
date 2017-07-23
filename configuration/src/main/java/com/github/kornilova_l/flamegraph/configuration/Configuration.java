package com.github.kornilova_l.flamegraph.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.TreeSet;

public class Configuration implements Cloneable {
    private Collection<MethodConfig> includingMethodConfigs;
    private Collection<MethodConfig> excludingMethodConfigs;

    public Configuration() {
        this(new TreeSet<>(), new TreeSet<>());
    }

    public Configuration clone() {
        try {
            return (Configuration) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
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

    public void maybeRemoveExactExcludingConfig(MethodConfig methodConfig) {
        excludingMethodConfigs.remove(methodConfig);
    }

    public void maybeRemoveExactIncludingConfig(MethodConfig methodConfig) {
        includingMethodConfigs.remove(methodConfig);
    }

    public boolean isMethodInstrumented(@NotNull MethodConfig methodConfig) {
        return getExcludingConfigs(methodConfig).size() == 0 &&
                getIncludingConfigs(methodConfig).size() != 0;
    }

    @NotNull
    public Collection<MethodConfig> getIncludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(includingMethodConfigs, methodConfig);
    }

    @NotNull
    public Collection<MethodConfig> getExcludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(excludingMethodConfigs, methodConfig);
    }

    @NotNull
    private static Collection<MethodConfig> getApplicableMethodConfigs(@NotNull Collection<MethodConfig> methodConfigs,
                                                                       @NotNull MethodConfig testedConfig) {
        Collection<MethodConfig> excludingConfigs = new TreeSet<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (methodConfig.isApplicableTo(testedConfig)) {
                excludingConfigs.add(methodConfig);
            }
        }
        return excludingConfigs;
    }

    public boolean isMethodExcluded(@NotNull MethodConfig methodConfig) {
        return getExcludingConfigs(methodConfig).size() != 0;
    }
}

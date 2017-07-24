package com.github.kornilova_l.flamegraph.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

public class Configuration implements Cloneable {
    private Set<MethodConfig> includingMethodConfigs;
    private Set<MethodConfig> excludingMethodConfigs;

    public Configuration() {
        this(new TreeSet<>(), new TreeSet<>());
    }

    /**
     * copy construction
     *
     * @param configuration configuration to copy
     */
    public Configuration(Configuration configuration) {
        includingMethodConfigs = new TreeSet<>();
        for (MethodConfig includingMethodConfig : configuration.includingMethodConfigs) {
            includingMethodConfigs.add(new MethodConfig(includingMethodConfig));
        }
        excludingMethodConfigs = new TreeSet<>();
        for (MethodConfig excludingMethodConfig : configuration.excludingMethodConfigs) {
            includingMethodConfigs.add(new MethodConfig(excludingMethodConfig));
        }
    }

    private Configuration(Set<MethodConfig> includingMethodConfigs, Set<MethodConfig> excludingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
        this.excludingMethodConfigs = excludingMethodConfigs;
    }

    public Set<MethodConfig> getIncludingMethodConfigs() {
        return includingMethodConfigs;
    }

    public void setIncludingMethodConfigs(Set<MethodConfig> includingMethodConfigs) {
        this.includingMethodConfigs = includingMethodConfigs;
    }

    public Set<MethodConfig> getExcludingMethodConfigs() {
        return excludingMethodConfigs;
    }

    public void setExcludingMethodConfigs(Set<MethodConfig> excludingMethodConfigs) {
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
    public Set<MethodConfig> getIncludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(includingMethodConfigs, methodConfig);
    }

    @NotNull
    public Set<MethodConfig> getExcludingConfigs(@NotNull MethodConfig methodConfig) {
        return getApplicableMethodConfigs(excludingMethodConfigs, methodConfig);
    }

    @NotNull
    private static Set<MethodConfig> getApplicableMethodConfigs(@NotNull Set<MethodConfig> methodConfigs,
                                                                @NotNull MethodConfig testedConfig) {
        Set<MethodConfig> excludingConfigs = new TreeSet<>();
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

    @NotNull
    public static MethodConfig getConfig(Set<MethodConfig> methodConfigs,
                                         String classNamePattern,
                                         String methodAndParamsPattern) {
        MethodConfig methodConfig = new MethodConfig(
                classNamePattern,
                methodAndParamsPattern.substring(0, methodAndParamsPattern.indexOf("(")),
                methodAndParamsPattern.substring(methodAndParamsPattern.indexOf("("), methodAndParamsPattern.length())
        );
        for (MethodConfig config : methodConfigs) {
            if (config.compareTo(methodConfig) == 0) {
                return config;
            }
        }
        throw new AssertionError("Could not find MethodConfig in configuration");
    }

    /**
     * copies links to fields of tempConfiguration
     *
     * @param tempConfiguration configuration from where links will be copied
     */
    public void assign(Configuration tempConfiguration) {
        includingMethodConfigs = tempConfiguration.includingMethodConfigs;
        excludingMethodConfigs = tempConfiguration.excludingMethodConfigs;
    }
}

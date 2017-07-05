package com.github.kornilova_l.plugin.config;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PublicField")
public class MethodConfig implements Comparable<MethodConfig> {

    public String qualifiedName;

    public MethodConfig() {
    }

    public MethodConfig(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String toString() {
        return qualifiedName;
    }

    @Override
    public int compareTo(@NotNull MethodConfig o) {
        return qualifiedName.compareTo(o.qualifiedName);
    }
}

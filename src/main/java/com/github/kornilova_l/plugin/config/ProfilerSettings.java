package com.github.kornilova_l.plugin.config;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PublicField")
public class ProfilerSettings implements Comparable<ProfilerSettings> {
    public ProfilerSettings() {
        this("");
    }

    @Override
    public String toString() {
        return name;
    }

    public ProfilerSettings(String name) {
        this.name = name;
    }

    public final String name;
    public String included = "";
    public String excluded = "";

    @Override
    public int compareTo(@NotNull ProfilerSettings o) {
        return o.name.compareTo(name);
    }
}

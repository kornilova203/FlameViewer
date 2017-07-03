package com.github.kornilova_l.plugin.config;

@SuppressWarnings("PublicField")
public class ProfilerSettings {
    public ProfilerSettings() {

    }

    @Override
    public String toString() {
        return name;
    }

    public ProfilerSettings(String name) {
        this.name = name;
    }

    public String name;
    public String included = "";
    public String excluded = "";
}

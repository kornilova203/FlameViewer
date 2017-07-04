package com.github.kornilova_l.plugin.config;

@SuppressWarnings("PublicField")
public class Config {
    public Config() {

    }

    @Override
    public String toString() {
        return name;
    }

    public Config(String name) {
        this.name = name;
    }

    public String name;
    public String included = "";
    public String excluded = "";

    public boolean isEnabled() {
        return true;
    }
}

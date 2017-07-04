package com.github.kornilova_l.plugin.config;

import java.util.Collection;

public class ConfigNode {
    public enum Type {
        METHOD,
        CLASS,
        PACKAGE
    }

    public String name;
    public Type type;
    public Collection<ConfigNode> children; // not for method type
    public Collection<Object> parameters; // for method type

    public ConfigNode() {
    }

    public ConfigNode(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }
}

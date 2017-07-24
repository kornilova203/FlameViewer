package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.ui.CheckedTreeNode;
import org.jetbrains.annotations.NotNull;

final public class ConfigCheckedTreeNode extends CheckedTreeNode {
    private final String name;
    private final MethodConfig methodConfig;

    ConfigCheckedTreeNode(@NotNull String name, MethodConfig methodConfig) {
        super(name);
        this.name = name;
        this.methodConfig = methodConfig;
    }

    @Override
    public String toString() {
        return name;
    }

    MethodConfig getMethodConfig() {
        return methodConfig;
    }
}
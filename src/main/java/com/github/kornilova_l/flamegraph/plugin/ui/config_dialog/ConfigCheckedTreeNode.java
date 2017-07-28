package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.ui.CheckedTreeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final public class ConfigCheckedTreeNode extends CheckedTreeNode {
    private String name;
    @Nullable
    private final MethodConfig methodConfig;

    ConfigCheckedTreeNode(@NotNull String name, @Nullable MethodConfig methodConfig) {
        super(name);
        this.name = name;
        this.methodConfig = methodConfig;
    }

    @Override
    public void setChecked(boolean checked) {
        if (methodConfig != null) {
            methodConfig.setEnabled(checked);
        }
        super.setChecked(checked);
    }

    @Override
    public String toString() {
        return name;
    }

    @Nullable MethodConfig getMethodConfig() {
        return methodConfig;
    }

    void setName(String name) {
        this.name = name;
    }
}
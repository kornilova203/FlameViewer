package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.ui.CheckedTreeNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ConfigCheckedTreeNode extends CheckedTreeNode {
    String name;
    @NotNull
    final List<MethodConfig> methodConfigs;

    ConfigCheckedTreeNode(@NotNull String name, @NotNull List<MethodConfig> methodConfigs) {
        super(name);
        this.name = name;
        this.methodConfigs = methodConfigs;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
    }

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void removeItselfFromConfigsSet();

    public enum TreeNodeType {
        PACKAGE,
        CLASS,
        METHOD
    }
}
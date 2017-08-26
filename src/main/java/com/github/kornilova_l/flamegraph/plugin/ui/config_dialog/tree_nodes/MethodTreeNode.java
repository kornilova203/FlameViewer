package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MethodTreeNode extends ConfigCheckedTreeNode {
    @NotNull
    private final MethodConfig methodConfig;

    public MethodTreeNode(@NotNull String name, @NotNull List<MethodConfig> methodConfigs, @NotNull MethodConfig methodConfig) {
        super(name, methodConfigs);
        this.methodConfig = methodConfig;
    }

    public List<MethodConfig> getMethodConfigs() {
        return methodConfigs;
    }

    @Override
    public void setChecked(boolean checked) {
        methodConfig.setEnabled(checked);
        super.setChecked(checked);
    }

    @Override
    public void removeItselfFromConfigsSet() {
        for (MethodConfig config : methodConfigs) {
            if (config == methodConfig) {
                methodConfigs.remove(config);
                return;
            }
        }
    }


    @NotNull
    public MethodConfig getMethodConfig() {
        return methodConfig;
    }
}

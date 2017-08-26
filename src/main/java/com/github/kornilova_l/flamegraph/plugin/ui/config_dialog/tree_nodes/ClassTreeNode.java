package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassTreeNode extends ConfigCheckedTreeNode {
    private final String classPatternString;

    public ClassTreeNode(@NotNull String name, @NotNull List<MethodConfig> methodConfigs,
                         @NotNull String packageName) {
        super(name, methodConfigs);
        this.classPatternString = packageName + "." + name;
    }

    @Override
    public void removeItselfFromConfigsSet() {
        List<MethodConfig> remove = new ArrayList<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.getClassPatternString(), classPatternString)) {
                remove.add(methodConfig);
            }
        }
        methodConfigs.removeAll(remove);
    }
}

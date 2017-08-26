package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PackageAndClassTreeNode extends ConfigCheckedTreeNode {
    public PackageAndClassTreeNode(@NotNull String name, @NotNull Set<MethodConfig> methodConfigs) {
        super(name, methodConfigs);
    }

    @Override
    public void removeItselfFromConfigsSet() {
        Set<MethodConfig> remove = new HashSet<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.getPackagePattern(), name)) {
                remove.add(methodConfig);
            }
        }
        methodConfigs.removeAll(remove);
    }
}

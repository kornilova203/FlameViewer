package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PackageAndClassTreeNode extends ConfigCheckedTreeNode {
    public PackageAndClassTreeNode(@NotNull String name, @NotNull List<MethodConfig> methodConfigs) {
        super(name, methodConfigs);
    }

    @Override
    public void removeItselfFromConfigsSet() {
        List<MethodConfig> remove = new ArrayList<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.getPackagePattern(), name)) {
                remove.add(methodConfig);
            }
        }
        methodConfigs.removeAll(remove);
    }
}

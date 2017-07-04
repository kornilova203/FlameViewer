package com.github.kornilova_l.plugin.config;

import com.intellij.ui.CheckedTreeNode;

public class ConfigItemNode extends CheckedTreeNode {
    private final ConfigItem config;

    ConfigItemNode(final ConfigItem config) {
        super(config);
        this.config = config;
        setChecked(config.isEnabled());
    }

    public ConfigItem getConfigItem() {
        return config;
    }
}

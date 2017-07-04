package com.github.kornilova_l.plugin.config;

import com.intellij.ui.CheckedTreeNode;

public class ConfigsGroupNode<G extends XConfigGroup> extends CheckedTreeNode {
    private final G myGroup;
    private final int myLevel;

    ConfigsGroupNode(G group, int level) {
        super(group);
        myLevel = level;
        setChecked(false);
        myGroup = group;
    }

    public G getGroup() {
        return myGroup;
    }

    public int getLevel() {
        return myLevel;
    }
}

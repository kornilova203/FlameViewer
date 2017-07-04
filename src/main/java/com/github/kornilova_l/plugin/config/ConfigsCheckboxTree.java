package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem;
import com.intellij.xdebugger.impl.breakpoints.ui.tree.*;

public class ConfigsCheckboxTree extends CheckboxTree {
    @Override
    protected void nodeStateWillChange(CheckedTreeNode node) {
        super.nodeStateWillChange(node);
        if (myDelegate != null) {
            myDelegate.nodeStateWillChange(node);
        }
    }

    @Override
    protected void onNodeStateChanged(CheckedTreeNode node) {
        super.onNodeStateChanged(node);
        if (myDelegate != null) {
            myDelegate.nodeStateDidChange(node);
        }
    }

    interface Delegate {
        void nodeStateDidChange(CheckedTreeNode node);

        void nodeStateWillChange(CheckedTreeNode node);
    }

    public void setDelegate(ConfigsCheckboxTree.Delegate delegate) {
        myDelegate = delegate;
    }

    private ConfigsCheckboxTree.Delegate myDelegate = null;

    public ConfigsCheckboxTree(Project project, ConfigItemsTreeController model) {
        super(new ConfigsTreeCellRenderer.ConfigsCheckboxTreeCellRenderer(project), model.getRoot());
        setHorizontalAutoScrollingEnabled(false);
    }

    @Override
    protected void installSpeedSearch() {
        new TreeSpeedSearch(this, path -> {
            Object node = path.getLastPathComponent();
            if (node instanceof BreakpointItemNode) {
                return ((BreakpointItemNode)node).getBreakpointItem().speedSearchText();
            }
            else if (node instanceof BreakpointsGroupNode) {
                return ((BreakpointsGroupNode)node).getGroup().getName();
            }
            return "";
        });
    }

    public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof BreakpointItemNode) {
            final BreakpointItem breakpointItem = ((BreakpointItemNode)value).getBreakpointItem();
            final String displayText = breakpointItem != null? breakpointItem.getDisplayText() : null;
            if (!StringUtil.isEmptyOrSpaces(displayText)) {
                return displayText;
            }
        }
        return super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
    }
}

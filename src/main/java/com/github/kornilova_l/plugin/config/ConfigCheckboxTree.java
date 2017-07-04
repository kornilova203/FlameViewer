package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Set;

public class ConfigCheckboxTree extends CheckboxTree {
    private static final class ConfigCheckedTreeNode extends CheckedTreeNode {
        @NotNull
        private final Config config;

        @NotNull
        public Config getConfig() {
            return config;
        }

        ConfigCheckedTreeNode(@NotNull Config config) {
            super(config);
            this.config = config;
        }
    }

    @NotNull
    private final CheckedTreeNode root;
    @NotNull
    private final DefaultTreeModel model;

    public ConfigCheckboxTree() {
        super(new CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof CheckedTreeNode)) return;
                CheckedTreeNode node = (CheckedTreeNode) value;

                final Color background = selected ? UIUtil.getTreeSelectionBackground() : UIUtil.getTreeTextBackground();
                boolean isPostfixTemplate = node instanceof ConfigCheckedTreeNode;
                SimpleTextAttributes attributes = isPostfixTemplate
                        ? SimpleTextAttributes.REGULAR_ATTRIBUTES
                        : SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
                getTextRenderer().append(StringUtil.notNullize(value.toString()),
                        new SimpleTextAttributes(background, attributes.getFgColor(), JBColor.RED, attributes.getStyle()));
            }
        }, new CheckedTreeNode(null));


        model = (DefaultTreeModel) getModel();
        root = (CheckedTreeNode) model.getRoot();

        getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(@NotNull TreeSelectionEvent event) {
                selectionChanged();
            }
        });
        setRootVisible(false);
        setShowsRootHandles(true);
    }

    protected void selectionChanged() {
        System.out.println("selection changed");
    }

    public void initTree(@NotNull Set<Config> configs) {
        root.removeAllChildren();
        for (Config config : configs) {
            CheckedTreeNode baseNode = new CheckedTreeNode("hello, " + config);
            root.add(baseNode);
            CheckedTreeNode configNode = new ConfigCheckedTreeNode(config);
            baseNode.add(configNode);
        }

        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        setSelectionRow(0);
    }

    @Nullable
    public Config getSelectedConfig() {
        TreePath path = getSelectionModel().getSelectionPath();
        Object userObject = ((CheckedTreeNode) path.getLastPathComponent()).getUserObject();
        System.out.println(userObject.getClass() + " " + userObject);
        if (userObject instanceof Config) {
            return ((Config) userObject);
        }
        return null;
    }
}

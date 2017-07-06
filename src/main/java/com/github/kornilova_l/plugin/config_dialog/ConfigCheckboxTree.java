package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.config.MethodConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ui.*;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Objects;
import java.util.Set;

public class ConfigCheckboxTree extends CheckboxTree {
    @NotNull
    private final CheckedTreeNode root;
    @NotNull
    private final DefaultTreeModel model;

    public ConfigCheckboxTree() {
        super(new CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (!(value instanceof ConfigCheckedTreeNode)) {
                    return;
                }
                ConfigCheckedTreeNode node = ((ConfigCheckedTreeNode) value);
                if (node.isRoot()) {
                    return;
                }
                if (((CheckedTreeNode) node.getParent()).isRoot()) {
                    this.getTextRenderer().setIcon(AllIcons.Nodes.Package);
                } else if (leaf) {
                    this.getTextRenderer().setIcon(AllIcons.Nodes.Method);
                } else {
                    this.getTextRenderer().setIcon(AllIcons.Nodes.Class);
                }
                this.getTextRenderer().append(value.toString(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
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

    private static ConfigCheckedTreeNode createChildIfNotPresent(CheckedTreeNode parent, String name) {
        ConfigCheckedTreeNode foundOrCreatedNode = null;
        if (parent.getChildCount() == 0) {
            foundOrCreatedNode = new ConfigCheckedTreeNode(name);
            parent.add(foundOrCreatedNode);
        } else {
            for (int i = 0; i < parent.getChildCount(); i++) {
                ConfigCheckedTreeNode child = (ConfigCheckedTreeNode) parent.getChildAt(i);
                if (Objects.equals(child.toString(), name)) {
                    foundOrCreatedNode = child;
                }
            }
        }
        if (foundOrCreatedNode == null) {
            foundOrCreatedNode = new ConfigCheckedTreeNode(name);
            parent.add(foundOrCreatedNode);
        }
        return foundOrCreatedNode;
    }

    public void initTree(@NotNull Set<MethodConfig> methods) {
        root.removeAllChildren();
        for (MethodConfig methodConfig : methods) {
            String name = methodConfig.qualifiedName;
            String methodName = name.substring(name.lastIndexOf(".") + 1, name.length());
            String rest = name.substring(0, name.lastIndexOf("."));
            String className = rest.substring(rest.lastIndexOf(".") + 1, rest.length());
            rest = rest.substring(0, rest.lastIndexOf("."));
            String packageName = rest;
            ConfigCheckedTreeNode packageNode = createChildIfNotPresent(root, packageName);
            ConfigCheckedTreeNode classNode = createChildIfNotPresent(packageNode, className);
            createChildIfNotPresent(classNode, methodName);
        }
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        setSelectionRow(0);
    }

    @Nullable
    public MethodConfig getSelectedConfig() {
        TreePath path = getSelectionModel().getSelectionPath();
        Object userObject = ((CheckedTreeNode) path.getLastPathComponent()).getUserObject();
        System.out.println(userObject.getClass() + " " + userObject);
        if (userObject instanceof MethodConfig) {
            return ((MethodConfig) userObject);
        }
        return null;
    }
}

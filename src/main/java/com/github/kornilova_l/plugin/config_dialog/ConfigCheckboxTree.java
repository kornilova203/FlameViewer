package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.config.MethodConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ui.*;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Collection;
import java.util.Objects;

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

        getSelectionModel().addTreeSelectionListener(event -> selectionChanged());
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

    public void initTree(@NotNull Collection<MethodConfig> methods) {
        root.removeAllChildren();
        for (MethodConfig methodConfig : methods) {
            addMethodNode(methodConfig);
        }
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        setSelectionRow(0);
    }

    @NotNull
    private ConfigCheckedTreeNode addMethodNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode packageNode = createChildIfNotPresent(root, methodConfig.getPackagePattern());
        ConfigCheckedTreeNode classNode = createChildIfNotPresent(packageNode, methodConfig.getClassPattern());
        return createChildIfNotPresent(classNode,
                (methodConfig.isExcluding ? "!" : "") +
                        methodConfig.methodPatternString +
                        methodConfig.parametersToStringForExport() +
                        (methodConfig.saveReturnValue ? "+" : ""));
    }

    @Nullable
    public MethodConfig getSelectedConfig() {
        TreePath path = getSelectionModel().getSelectionPath();
        if (path == null) {
            return null;
        }
        Object userObject = ((CheckedTreeNode) path.getLastPathComponent()).getUserObject();
        System.out.println(userObject.getClass() + " " + userObject);
        if (userObject instanceof MethodConfig) {
            return ((MethodConfig) userObject);
        }
        return null;
    }

    public void addNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = addMethodNode(methodConfig);
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        getSelectionModel().setSelectionPath(new TreePath(newNode.getPath()));
    }
}

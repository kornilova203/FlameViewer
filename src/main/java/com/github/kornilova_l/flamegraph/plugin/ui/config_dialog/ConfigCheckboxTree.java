package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConfigCheckboxTree extends CheckboxTree {
    public final TreeType treeType;
    private final MethodFormManager methodFormManager;
    @NotNull
    private final CheckedTreeNode root;
    @NotNull
    private final DefaultTreeModel model;
    @Nullable
    ConfigCheckedTreeNode nodeWithBadValidation = null;

    ConfigCheckboxTree(JPanel cardPanel,
                       ExcludedMethodForm excludedMethodForm,
                       @Nullable JCheckBox saveReturnValueCheckBox,
                       Set<MethodConfig> methodConfigs,
                       TreeType treeType) {
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
        methodFormManager = new MethodFormManager(cardPanel, excludedMethodForm, saveReturnValueCheckBox, methodConfigs, this);

        this.treeType = treeType;
        model = (DefaultTreeModel) getModel();
        root = (CheckedTreeNode) model.getRoot();

        getSelectionModel().addTreeSelectionListener(this::selectionChanged);
        setRootVisible(false);
        setShowsRootHandles(true);
    }

    private ConfigCheckedTreeNode createChildIfNotPresent(CheckedTreeNode parent,
                                                          String name,
                                                          @Nullable MethodConfig methodConfig) {
        if (parent.getChildCount() == 0) {
            return appendNode(parent, name, methodConfig);
        } else {
            ConfigCheckedTreeNode node = maybeInsertOrFind(parent, name, methodConfig);
            if (node != null) {
                return node;
            }
        }
        return appendNode(parent, name, methodConfig);
    }

    @Nullable
    private ConfigCheckedTreeNode maybeInsertOrFind(CheckedTreeNode parent, String name, @Nullable MethodConfig methodConfig) {
        if (name.compareTo(parent.getChildAt(0).toString()) < 0) {
            return insertBefore(parent, parent.getChildAt(0), name, methodConfig);
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            ConfigCheckedTreeNode child = (ConfigCheckedTreeNode) parent.getChildAt(i);
            TreeNode nextNode = parent.getChildAfter(child);
            if (nextNode != null) {
                if (name.compareTo(child.toString()) > 0 &&
                        name.compareTo(nextNode.toString()) < 0) {
                    return insertBefore(parent, nextNode, name, methodConfig);
                }
            }
            if (Objects.equals(child.toString(), name)) {
                return child;
            }
        }
        return null;
    }

    private ConfigCheckedTreeNode appendNode(CheckedTreeNode parent, String name, @Nullable MethodConfig methodConfig) {
        ConfigCheckedTreeNode node = new ConfigCheckedTreeNode(name, methodConfig);
        model.insertNodeInto(node, parent, parent.getChildCount());
        model.nodeStructureChanged(parent);
        return node;
    }

    @NotNull
    private ConfigCheckedTreeNode insertBefore(CheckedTreeNode parent,
                                               TreeNode child,
                                               String name,
                                               @Nullable MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = new ConfigCheckedTreeNode(name, methodConfig);
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (Objects.equals(parent.getChildAt(i).toString(), child.toString())) {
                model.insertNodeInto(newNode, parent, i);
                model.nodeStructureChanged(parent);
                return newNode;
            }
        }
        throw new RuntimeException("Cannot insert new node");
    }

    @NotNull
    List<ValidationInfo> validateInfo() {
        return methodFormManager.validateInfo();
    }

    public void updateTreeNodeNames(Object[] path) {
        ConfigCheckedTreeNode checkedTreeNode = (ConfigCheckedTreeNode) path[path.length - 1];
        MethodConfig methodConfig = checkedTreeNode.getMethodConfig();
        checkedTreeNode.setName(methodConfig.getMethodPatternString() + methodConfig.parametersToString());
        model.nodeChanged(checkedTreeNode);
        ((ConfigCheckedTreeNode) path[path.length - 2]).setName(methodConfig.getClassPattern());
        model.nodeChanged((CheckedTreeNode) path[path.length - 2]);
        ((ConfigCheckedTreeNode) path[path.length - 3]).setName(methodConfig.getPackagePattern());
        model.nodeChanged((CheckedTreeNode) path[path.length - 3]);
    }

    private void selectionChanged(TreeSelectionEvent event) {
        if (nodeWithBadValidation != null) {
            if (event.getPath().getLastPathComponent() != nodeWithBadValidation) {
                setSelectionPath(event.getOldLeadSelectionPath());
            }
        } else {
            TreePath oldPath = event.getOldLeadSelectionPath();
            if (oldPath != null) {
                Object[] path = oldPath.getPath();
                if (path.length == 4) {
                    updateTreeNodeNames(path);
                }
            }
            methodFormManager.selectionChanged(event.getPath());
        }
    }

    void initTree(@NotNull Collection<MethodConfig> including) {
        root.removeAllChildren();
        for (MethodConfig methodConfig : including) {
            addMethodNode(methodConfig);
        }
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        setSelectionRow(0);
    }

    private void addMethodNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode packageNode = createChildIfNotPresent(root, methodConfig.getPackagePattern(), null);
        ConfigCheckedTreeNode classNode = createChildIfNotPresent(packageNode, methodConfig.getClassPattern(), null);
        createChildIfNotPresent(classNode,
                methodConfig.getMethodPatternString()
                        + methodConfig.parametersToString(),
                methodConfig);
    }

    @Nullable
    public MethodConfig getSelectedConfig() {
        ConfigCheckedTreeNode node = getSelectedNode();
        if (node != null) {
            return node.getMethodConfig();
        }
        return null;
    }

    @Nullable
    public ConfigCheckedTreeNode getSelectedNode() {
        TreePath treePath = getSelectionModel().getSelectionPath();
        if (treePath == null) {
            return null;
        }
        Object[] path = treePath.getPath();
        return (ConfigCheckedTreeNode) path[path.length - 1];
    }

    public void addNode(MethodConfig methodConfig) {
        addMethodNode(methodConfig);
        TreeUtil.expandAll(this);
    }

    public void removeNode(@NotNull ConfigCheckedTreeNode treeNode) {
        CheckedTreeNode classNode = ((CheckedTreeNode) treeNode.getParent());
        CheckedTreeNode packageNode = ((CheckedTreeNode) classNode.getParent());
        if (packageNode.getChildCount() == 1 && classNode.getChildCount() == 1) {
            int index = root.getIndex(packageNode);
            if (index != -1) {
                root.remove(index);
            }
        } else if (classNode.getChildCount() == 1) {
            packageNode.remove(packageNode.getIndex(classNode));
        } else {
            classNode.remove(classNode.getIndex(treeNode));
        }
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
    }

    public enum TreeType {
        INCLUDING,
        EXCLUDING
    }
}
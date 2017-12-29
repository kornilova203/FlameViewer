package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ClassTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ConfigCheckedTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ConfigCheckedTreeNode.TreeNodeType;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.MethodTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.PackageAndClassTreeNode;
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
import java.util.List;
import java.util.Objects;

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
                       List<MethodConfig> methodConfigs,
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
                                                          @NotNull List<MethodConfig> methodConfigs,
                                                          @NotNull MethodConfig methodConfig,
                                                          TreeNodeType nodeType) {
        if (parent.getChildCount() == 0) {
            return appendNode(parent, methodConfig, nodeType, methodConfigs);
        } else {
            ConfigCheckedTreeNode node = maybeInsertOrFind(parent, methodConfigs, methodConfig, nodeType);
            if (node != null) {
                return node;
            }
        }
        return appendNode(parent, methodConfig, nodeType, methodConfigs);
    }

    @Nullable
    private ConfigCheckedTreeNode maybeInsertOrFind(CheckedTreeNode parent,
                                                    @NotNull List<MethodConfig> methodConfigs,
                                                    @NotNull MethodConfig methodConfig,
                                                    TreeNodeType nodeType) {
        String name = getName(methodConfig, nodeType);
        if (name.compareTo(parent.getChildAt(0).toString()) < 0) {
            return insertBefore(parent, parent.getChildAt(0), methodConfigs, methodConfig, nodeType);
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            ConfigCheckedTreeNode child = (ConfigCheckedTreeNode) parent.getChildAt(i);
            TreeNode nextNode = parent.getChildAfter(child);
            if (nextNode != null) {
                if (name.compareTo(child.toString()) > 0 &&
                        name.compareTo(nextNode.toString()) < 0) {
                    return insertBefore(parent, nextNode, methodConfigs, methodConfig, nodeType);
                }
            }
            if (Objects.equals(child.toString(), name)) {
                return child;
            }
        }
        return null;
    }

    @NotNull
    private ConfigCheckedTreeNode insertNode(CheckedTreeNode parent,
                                             @NotNull List<MethodConfig> methodConfigs,
                                             @NotNull MethodConfig methodConfig,
                                             TreeNodeType nodeType) {
        if (parent.getChildCount() == 0) {
            return appendNode(parent, methodConfig, nodeType, methodConfigs);
        }
        String name = getName(methodConfig, nodeType);
        if (name.compareTo(parent.getChildAt(0).toString()) < 0) {
            return insertBefore(parent, parent.getChildAt(0), methodConfigs, methodConfig, nodeType);
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            ConfigCheckedTreeNode child = (ConfigCheckedTreeNode) parent.getChildAt(i);
            TreeNode nextNode = parent.getChildAfter(child);
            if (nextNode != null) {
                if (name.compareTo(child.toString()) > 0 &&
                        name.compareTo(nextNode.toString()) < 0) {
                    return insertBefore(parent, nextNode, methodConfigs, methodConfig, nodeType);
                }
            }
        }
        return appendNode(parent, methodConfig, nodeType, methodConfigs);
    }

    @NotNull
    private String getName(@NotNull MethodConfig methodConfig, TreeNodeType nodeType) {
        switch (nodeType) {
            case PACKAGE:
                return methodConfig.getPackagePattern();
            case CLASS:
                return methodConfig.getClassPattern();
            case METHOD:
                return methodConfig.getMethodPatternString() + methodConfig.parametersToString();
        }
        throw new AssertionError("NodeType is not known");
    }

    private ConfigCheckedTreeNode appendNode(CheckedTreeNode parent,
                                             @NotNull MethodConfig methodConfig,
                                             TreeNodeType nodeType,
                                             List<MethodConfig> methodConfigs) {
        ConfigCheckedTreeNode node = createNode(methodConfigs, methodConfig, nodeType);
        model.insertNodeInto(node, parent, parent.getChildCount());
        model.nodeStructureChanged(parent);
        return node;
    }

    @NotNull
    private ConfigCheckedTreeNode createNode(@NotNull List<MethodConfig> methodConfigs,
                                             @NotNull MethodConfig methodConfig,
                                             TreeNodeType nodeType) {
        String name = getName(methodConfig, nodeType);
        switch (nodeType) {
            case PACKAGE:
                return new PackageAndClassTreeNode(name, methodConfigs);
            case CLASS:
                return new ClassTreeNode(name, methodConfigs, methodConfig.getPackagePattern());
            case METHOD:
                ConfigCheckedTreeNode node;
                node = new MethodTreeNode(name, methodConfigs, methodConfig);
                node.setChecked(methodConfig.isEnabled());
                return node;
            default:
                return null;
        }
    }

    @NotNull
    private ConfigCheckedTreeNode insertBefore(CheckedTreeNode parent,
                                               TreeNode child,
                                               @NotNull List<MethodConfig> methodConfigs,
                                               @NotNull MethodConfig methodConfig,
                                               TreeNodeType nodeType) {
        ConfigCheckedTreeNode newNode = createNode(methodConfigs, methodConfig, nodeType);
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
        if (checkedTreeNode == null || !(checkedTreeNode instanceof MethodTreeNode)) {
            return;
        }
        MethodConfig methodConfig = ((MethodTreeNode) checkedTreeNode).getMethodConfig();
        removeNode(checkedTreeNode);
        addMethodNode(methodConfig, ((MethodTreeNode) checkedTreeNode).getMethodConfigs());
        TreeUtil.expandAll(this);
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
                if (path.length == 4 && areNamesChanged(path)) {
                    updateTreeNodeNames(path);
                }
            }
            methodFormManager.selectionChanged(event.getPath());
        }
    }

    /**
     * @param path that contains 4 nodes
     */
    private boolean areNamesChanged(Object[] path) {
        ConfigCheckedTreeNode methodNode = (ConfigCheckedTreeNode) path[3];
        if (methodNode == null || !(methodNode instanceof MethodTreeNode)) {
            return false;
        }
        MethodConfig methodConfig = ((MethodTreeNode) methodNode).getMethodConfig();
        return !methodNode.getName().equals(methodConfig.getMethodPatternString() + methodConfig.parametersToString()) ||
                !((ConfigCheckedTreeNode) path[2]).getName().equals(methodConfig.getClassPattern()) ||
                !((ConfigCheckedTreeNode) path[1]).getName().equals(methodConfig.getPackagePattern());
    }

    public void initTree(@NotNull List<MethodConfig> methodConfigs) {
        root.removeAllChildren();
        for (MethodConfig methodConfig : methodConfigs) {
            addMethodNode(methodConfig, methodConfigs);
        }
        model.nodeStructureChanged(root);
        setNodesChecked(root);
        TreeUtil.expandAll(this);
        setSelectionRow(0);
    }

    private void setNodesChecked(@NotNull CheckedTreeNode node) {
        if (!node.isRoot() && !node.isLeaf()) {
            if (hasAnyCheckedLeaf(node)) {
                node.setChecked(true);
            } else {
                node.setChecked(false);
            }
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            setNodesChecked(((CheckedTreeNode) node.getChildAt(i)));
        }
    }

    private boolean hasAnyCheckedLeaf(@NotNull CheckedTreeNode node) {
        if (node instanceof MethodTreeNode) {
            MethodConfig methodConfig = ((MethodTreeNode) node).getMethodConfig();
            return methodConfig.isEnabled();
        }
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (hasAnyCheckedLeaf(((CheckedTreeNode) node.getChildAt(i)))) {
                return true;
            }
        }
        return false;
    }

    private MethodTreeNode addMethodNode(MethodConfig methodConfig, List<MethodConfig> methodConfigs) {
        ConfigCheckedTreeNode packageNode = createChildIfNotPresent(
                root, methodConfigs, methodConfig, TreeNodeType.PACKAGE);
        ConfigCheckedTreeNode classNode = createChildIfNotPresent(
                packageNode, methodConfigs, methodConfig, TreeNodeType.CLASS);
        return ((MethodTreeNode) insertNode(classNode, methodConfigs, methodConfig, TreeNodeType.METHOD));
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

    public MethodTreeNode addNode(MethodConfig methodConfig, @NotNull List<MethodConfig> methodConfigs) {
        return addMethodNode(methodConfig, methodConfigs);
    }

    public void removeNode(@NotNull ConfigCheckedTreeNode treeNode) {
        CheckedTreeNode parent = (CheckedTreeNode) treeNode.getParent();
        removeChild(treeNode);
        while (parent != root && parent.getChildCount() == 0) {
            CheckedTreeNode tempParent = ((CheckedTreeNode) parent.getParent());
            removeChild(parent);
            parent = tempParent;
        }
        model.nodeStructureChanged(root);
        methodFormManager.showChooseConfig();
        TreeUtil.expandAll(this);
    }

    private void removeChild(@NotNull CheckedTreeNode child) {
        CheckedTreeNode parent = ((CheckedTreeNode) child.getParent());
        int index = parent.getIndex(child);
        if (index != -1) {
            parent.remove(index);
        }
    }

    @Nullable
    public MethodConfig getSelectedConfig() {
        ConfigCheckedTreeNode node = getSelectedNode();
        if (!(node instanceof MethodTreeNode)) {
            return null;
        }
        return ((MethodTreeNode) node).getMethodConfig();
    }

    public void duplicate(MethodTreeNode node) {
        MethodConfig methodConfig = new MethodConfig(node.getMethodConfig());
        node.getMethodConfigs().add(methodConfig);
        MethodTreeNode newNode = addNode(methodConfig, node.getMethodConfigs());
        setSelectionPath(new TreePath(newNode.getPath()));
    }

    public void setSelected(MethodConfig methodConfig) {
        MethodTreeNode node = findNodeRecursively(root, methodConfig);
        if (node == null) {
            return;
        }
        setSelectionPath(new TreePath(node.getPath()));
    }

    private MethodTreeNode findNodeRecursively(@NotNull CheckedTreeNode node, MethodConfig methodConfig) {
        int len = node.getChildCount();
        if (node instanceof MethodTreeNode) {
            if (Objects.equals(((MethodTreeNode) node).getMethodConfig().toString(), methodConfig.toString())) {
                return ((MethodTreeNode) node);
            }
        }
        for (int i = 0; i < len; i++) {
            MethodTreeNode res = findNodeRecursively((CheckedTreeNode) node.getChildAt(i), methodConfig);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public enum TreeType {
        INCLUDING,
        EXCLUDING
    }
}
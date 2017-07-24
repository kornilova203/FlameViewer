package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

class ConfigCheckboxTree extends CheckboxTree {
    @NotNull
    private final CheckedTreeNode root;
    @NotNull
    private final DefaultTreeModel model;
    private MethodForm methodForm;
    private Set<MethodConfig> methodConfigs;
    private MyDocumentListener methodDocumentListener;
    private MyDocumentListener classDocumentListener;

    ConfigCheckboxTree(MethodForm methodForm, Set<MethodConfig> methodConfigs) {
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
        this.methodForm = methodForm;
        this.methodConfigs = methodConfigs;


        model = (DefaultTreeModel) getModel();
        root = (CheckedTreeNode) model.getRoot();

        getSelectionModel().addTreeSelectionListener(this::selectionChanged);
        setRootVisible(false);
        setShowsRootHandles(true);

        setDocumentsListeners();
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

    private void setDocumentsListeners() {
        methodDocumentListener = new MyDocumentListener(
                MyDocumentListener.FieldType.METHOD,
                methodConfigs
        );
        classDocumentListener = new MyDocumentListener(
                MyDocumentListener.FieldType.CLASS,
                methodConfigs
        );
    }

    private ConfigCheckedTreeNode createMethodNode(ConfigCheckedTreeNode classNode, String name, MethodConfig methodConfig) {
        ConfigCheckedTreeNode treeNode = new ConfigCheckedTreeNode(name, methodConfig);
        classNode.add(treeNode);
        return treeNode;
    }

    private void selectionChanged(TreeSelectionEvent event) {
        methodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
        methodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
        TreePath treePath = event.getPath();
        if (treePath.getPathCount() < 4) {
            methodForm.methodNamePatternTextField.setText("");
            methodForm.classNamePatternTextField.setText("");
        } else {
            MethodConfig methodConfig = getSelectedConfig();
            if (methodConfig == null) {
                return;
            }
            methodForm.methodNamePatternTextField.setText(methodConfig.getMethodPatternString());
            methodForm.classNamePatternTextField.setText(methodConfig.getClassPatternString());
            methodForm.methodNamePatternTextField.addFocusListener(
                    new MyFocusListener(
                            (ConfigCheckedTreeNode) treePath.getLastPathComponent(),
                            methodConfig,
                            this
                    )
            );
            methodDocumentListener.setCurrentMethodConfig(methodConfig);
            methodDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
            classDocumentListener.setCurrentMethodConfig(methodConfig);
            classDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
            methodForm.classNamePatternTextField.addFocusListener(
                    new MyFocusListener(
                            (ConfigCheckedTreeNode) treePath.getLastPathComponent(),
                            methodConfig,
                            this
                    )
            );
            methodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
            methodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
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

    private ConfigCheckedTreeNode addMethodNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode packageNode = createChildIfNotPresent(root, methodConfig.getPackagePattern());
        ConfigCheckedTreeNode classNode = createChildIfNotPresent(packageNode, methodConfig.getClassPattern());
        return createMethodNode(classNode,
                methodConfig.getMethodPatternString()
                        + methodConfig.parametersToString() +
                        (methodConfig.isSaveReturnValue() ? "+" : ""),
                methodConfig);
    }

    @Nullable
    private MethodConfig getSelectedConfig() {
        TreePath path = getSelectionModel().getSelectionPath();
        if (path == null) {
            return null;
        }
        Object lastPathComponent = path.getLastPathComponent();
        if (lastPathComponent instanceof ConfigCheckedTreeNode) {
            MethodConfig methodConfig = ((ConfigCheckedTreeNode) lastPathComponent).getMethodConfig();
            System.out.println(methodConfig.getClass() + " " + methodConfig);
            return methodConfig;
        }
        return null;
    }

    void addNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = addMethodNode(methodConfig);
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        getSelectionModel().setSelectionPath(new TreePath(newNode.getPath()));
    }

    void removeNode(@NotNull ConfigCheckedTreeNode treeNode) {
        CheckedTreeNode classNode = ((CheckedTreeNode) treeNode.getParent());
        CheckedTreeNode packageNode = ((CheckedTreeNode) classNode.getParent());
        if (packageNode.getChildCount() == 1) {
            packageNode.removeAllChildren();
        }
        if (classNode.getChildCount() == 1) {
            classNode.removeAllChildren();
        } else {
            classNode.remove(classNode.getIndex(treeNode));
        }
    }
}

class MyDocumentListener implements DocumentListener {
    private final FieldType fieldType;
    private Collection<MethodConfig> methodConfigs;
    @Nullable
    private MethodConfig currentMethodConfig = null;
    @Nullable
    private ConfigCheckedTreeNode treeNode = null;

    MyDocumentListener(FieldType fieldType, Collection<MethodConfig> methodConfigs) {
        this.fieldType = fieldType;
        this.methodConfigs = methodConfigs;
    }

    void setCurrentMethodConfig(@Nullable MethodConfig currentMethodConfig) {
        this.currentMethodConfig = currentMethodConfig;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateConfig(e);
    }

    private void updateConfig(DocumentEvent e) {
        if (currentMethodConfig != null && treeNode != null) {
            try {
                methodConfigs.remove(currentMethodConfig);

                switch (fieldType) {
                    case CLASS:
                        currentMethodConfig.setClassPatternString(e.getDocument().getText(0, e.getDocument().getLength()));
                        break;
                    case METHOD:
                        currentMethodConfig.setMethodPatternString(e.getDocument().getText(0, e.getDocument().getLength()));
                }
                methodConfigs.add(currentMethodConfig);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateConfig(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    void setTreeNode(@Nullable ConfigCheckedTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    enum FieldType {
        METHOD,
        CLASS
    }
}

class MyFocusListener implements FocusListener {

    private final ConfigCheckedTreeNode checkedTreeNode;
    private final MethodConfig methodConfig;
    private ConfigCheckboxTree tree;

    MyFocusListener(ConfigCheckedTreeNode checkedTreeNode, MethodConfig methodConfig, ConfigCheckboxTree tree) {

        this.checkedTreeNode = checkedTreeNode;
        this.methodConfig = methodConfig;
        this.tree = tree;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        tree.clearSelection();
        tree.removeNode(checkedTreeNode);
        tree.addNode(methodConfig);
    }
}
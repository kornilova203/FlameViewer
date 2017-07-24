package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
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
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class ConfigCheckboxTree extends CheckboxTree {
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

    private void selectionChanged(TreeSelectionEvent event) {
        TreePath treePath = event.getPath();
        if (treePath.getPathCount() < 4) {
            methodForm.methodNamePatternTextField.setText("");
            methodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
            methodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
        } else {
            String classNamePattern = getClassNamePattern(treePath);
            String methodAndParametersPattern = treePath.getLastPathComponent().toString();
            methodForm.methodNamePatternTextField.setText(methodAndParametersPattern.substring(0, methodAndParametersPattern.indexOf("(")));
            methodForm.classNamePatternTextField.setText(classNamePattern);
            MethodConfig currentMethodConfig = Configuration.getConfig(methodConfigs, classNamePattern, methodAndParametersPattern);
            methodDocumentListener.setCurrentMethodConfig(currentMethodConfig);
            classDocumentListener.setCurrentMethodConfig(currentMethodConfig);
            methodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
            methodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
        }
    }

    private String getClassNamePattern(TreePath path) {
        return path.getPathComponent(1) + "." + path.getPathComponent(2);
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
        return createChildIfNotPresent(classNode,
                        methodConfig.getMethodPatternString()
                        + methodConfig.parametersToString() +
                        (methodConfig.isSaveReturnValue() ? "+" : ""));
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

    void addNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = addMethodNode(methodConfig);
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        getSelectionModel().setSelectionPath(new TreePath(newNode.getPath()));
    }
}

class MyDocumentListener implements DocumentListener {
    private final FieldType fieldType;
    private Collection<MethodConfig> methodConfigs;
    @Nullable
    private MethodConfig currentMethodConfig = null;

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
        if (currentMethodConfig != null) {
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

    enum FieldType {
        METHOD,
        CLASS
    }
}
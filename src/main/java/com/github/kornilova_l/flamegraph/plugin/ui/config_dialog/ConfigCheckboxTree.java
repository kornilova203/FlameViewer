package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
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
    private JPanel cardPanel;
    private MethodForm methodForm;
    private Set<MethodConfig> methodConfigs;
    private TreeType treeType;
    private MyDocumentListener methodDocumentListener;
    private MyDocumentListener classDocumentListener;
    private MyFocusListener myFocusListener;

    ConfigCheckboxTree(JPanel cardPanel, MethodForm methodForm, Set<MethodConfig> methodConfigs, TreeType treeType) {
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
        this.cardPanel = cardPanel;
        this.methodForm = methodForm;
        this.methodConfigs = methodConfigs;
        this.treeType = treeType;


        model = (DefaultTreeModel) getModel();
        root = (CheckedTreeNode) model.getRoot();

        getSelectionModel().addTreeSelectionListener(this::selectionChanged);
        setRootVisible(false);
        setShowsRootHandles(true);

        setDocumentsListeners();
        addParamsTable();
    }

    private static ConfigCheckedTreeNode createChildIfNotPresent(CheckedTreeNode parent,
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
    private static ConfigCheckedTreeNode maybeInsertOrFind(CheckedTreeNode parent, String name, @Nullable MethodConfig methodConfig) {
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

    private static ConfigCheckedTreeNode appendNode(CheckedTreeNode parent, String name, @Nullable MethodConfig methodConfig) {
        ConfigCheckedTreeNode node = new ConfigCheckedTreeNode(name, methodConfig);
        parent.add(node);
        return node;
    }

    @NotNull
    private static ConfigCheckedTreeNode insertBefore(CheckedTreeNode parent,
                                                      TreeNode child,
                                                      String name,
                                                      @Nullable MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = new ConfigCheckedTreeNode(name, methodConfig);
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (Objects.equals(parent.getChildAt(i).toString(), child.toString())) {
                parent.insert(newNode, i);
                return newNode;
            }
        }
        throw new RuntimeException("Cannot insert new node");
    }

    private void addParamsTable() {
        String[] columnNames = {"Type", "Save"};
        Object[][] data = {
                {"String", true},
                {"int", false}
        };
        JTable myTable = new JBTable(new DefaultTableModel(data, columnNames)) {
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return Boolean.class;
                    default:
                        return String.class;
                }
            }
        };
        JBScrollPane scrollPane = new JBScrollPane(myTable);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        methodForm.paramTablePanel.add(scrollPane,
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

    private void selectionChanged(TreeSelectionEvent event) {
        methodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
        methodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
        if (myFocusListener != null) {
            methodForm.methodNamePatternTextField.removeFocusListener(myFocusListener);
            methodForm.classNamePatternTextField.removeFocusListener(myFocusListener);
        }
        TreePath treePath = event.getPath();
        if (treePath.getPathCount() < 4) {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.EMPTY_CARD_KEY);
        } else {
            showMethodForm(treePath);
        }
    }

    private void showMethodForm(TreePath treePath) {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.FORM_CARD_KEY);
        MethodConfig methodConfig = getSelectedConfig();
        myFocusListener = new MyFocusListener(
                (ConfigCheckedTreeNode) treePath.getLastPathComponent(),
                methodConfig,
                this
        );
        if (methodConfig == null) {
            return;
        }
        methodForm.methodNamePatternTextField.setText(methodConfig.getMethodPatternString());
        methodForm.classNamePatternTextField.setText(methodConfig.getClassPatternString());
        methodDocumentListener.setCurrentMethodConfig(methodConfig);
        classDocumentListener.setCurrentMethodConfig(methodConfig);
        methodDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        classDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        methodForm.classNamePatternTextField.addFocusListener(myFocusListener);
        methodForm.methodNamePatternTextField.addFocusListener(myFocusListener);
        methodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
        methodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
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
        ConfigCheckedTreeNode packageNode = createChildIfNotPresent(root, methodConfig.getPackagePattern(), null);
        ConfigCheckedTreeNode classNode = createChildIfNotPresent(packageNode, methodConfig.getClassPattern(), null);
        return createChildIfNotPresent(classNode,
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
            return ((ConfigCheckedTreeNode) lastPathComponent).getMethodConfig();
        }
        return null;
    }

    @NotNull
    ConfigCheckedTreeNode addNode(MethodConfig methodConfig) {
        ConfigCheckedTreeNode newNode = addMethodNode(methodConfig);
        model.nodeStructureChanged(root);
        TreeUtil.expandAll(this);
        return newNode;
    }

    void removeNode(@NotNull ConfigCheckedTreeNode treeNode) {
        CheckedTreeNode classNode = ((CheckedTreeNode) treeNode.getParent());
        CheckedTreeNode packageNode = ((CheckedTreeNode) classNode.getParent());
        if (packageNode.getChildCount() == 1) {
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
    }

    enum TreeType {
        INCLUDING,
        EXCLUDING
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

    private final MethodConfig methodConfig;
    @NotNull
    private ConfigCheckedTreeNode checkedTreeNode;
    private ConfigCheckboxTree tree;

    MyFocusListener(@NotNull ConfigCheckedTreeNode checkedTreeNode, MethodConfig methodConfig, ConfigCheckboxTree tree) {
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
        checkedTreeNode = tree.addNode(methodConfig);
        tree.getSelectionModel().setSelectionPath(new TreePath(checkedTreeNode.getPath()));
    }
}
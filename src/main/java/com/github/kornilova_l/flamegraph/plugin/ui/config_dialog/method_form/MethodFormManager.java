package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckedTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigurationForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ExcludedMethodForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove.DialogHelper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MyTableView.createTablePanel;

public class MethodFormManager {
    private final JPanel cardPanel;
    @NotNull
    private final ExcludedMethodForm excludedMethodForm;
    private final Set<MethodConfig> methodConfigs;
    @Nullable
    private JCheckBox saveReturnValueCheckBox;
    private ConfigCheckboxTree tree;
    private MyDocumentListener methodDocumentListener;
    private MyDocumentListener classDocumentListener;
    private MyFocusListener myFocusListener;
    private ChangeListener checkboxChangeListener;
    private Map<MethodConfig, String> methodKeysMap = new HashMap<>();
    private String latestMethodKey = "";
    private JPanel currentTablePanel = null;
    private List<MethodConfig.Parameter> currentParameters = null;

    public MethodFormManager(JPanel cardPanel,
                             @NotNull ExcludedMethodForm excludedMethodForm,
                             @Nullable JCheckBox saveReturnValueCheckBox,
                             Set<MethodConfig> methodConfigs,
                             ConfigCheckboxTree tree) {
        this.cardPanel = cardPanel;
        this.excludedMethodForm = excludedMethodForm;
        this.saveReturnValueCheckBox = saveReturnValueCheckBox;
        this.methodConfigs = methodConfigs;
        this.tree = tree;
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

    public void selectionChanged(TreePath path) {
        excludedMethodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
        excludedMethodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
        if (myFocusListener != null) {
            excludedMethodForm.methodNamePatternTextField.removeFocusListener(myFocusListener);
            excludedMethodForm.classNamePatternTextField.removeFocusListener(myFocusListener);
        }
        if (saveReturnValueCheckBox != null && checkboxChangeListener != null) {
            saveReturnValueCheckBox.removeChangeListener(checkboxChangeListener);
        }
        if (path.getPathCount() < 4) {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.EMPTY_CARD_KEY);
        } else {
            showMethodForm(path);
        }
    }

    private void showMethodForm(TreePath treePath) {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.FORM_CARD_KEY);
        MethodConfig methodConfig = tree.getSelectedConfig();
        if (methodConfig == null) {
            return;
        }
        String key = methodKeysMap.computeIfAbsent(methodConfig, MethodConfig::toString);
        if (!Objects.equals(latestMethodKey, key)) {
            currentTablePanel = createTablePanel(methodConfig.getParameters(), tree.treeType);
            currentParameters = methodConfig.getParameters();
            excludedMethodForm.paramTableCards.add(
                    currentTablePanel,
                    key
            );
            ((CardLayout) excludedMethodForm.paramTableCards.getLayout()).show(excludedMethodForm.paramTableCards, key);
            myFocusListener = new MyFocusListener(treePath.getPath(), tree);
        }
        latestMethodKey = key;
        excludedMethodForm.methodNamePatternTextField.setText(methodConfig.getMethodPatternString());
        excludedMethodForm.classNamePatternTextField.setText(methodConfig.getClassPatternString());
        methodDocumentListener.setCurrentMethodConfig(methodConfig);
        classDocumentListener.setCurrentMethodConfig(methodConfig);
        methodDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        classDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        excludedMethodForm.classNamePatternTextField.addFocusListener(myFocusListener);
        excludedMethodForm.methodNamePatternTextField.addFocusListener(myFocusListener);
        excludedMethodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
        excludedMethodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
        if (saveReturnValueCheckBox != null) {
            saveReturnValueCheckBox.setSelected(methodConfig.isSaveReturnValue());
            checkboxChangeListener = e -> methodConfig.setSaveReturnValue(!methodConfig.isSaveReturnValue());
            saveReturnValueCheckBox.addChangeListener(checkboxChangeListener);
        }
    }

    @NotNull
    public List<ValidationInfo> validateInfo() {
        List<ValidationInfo> validationInfos = new LinkedList<>();
        if (tree.getSelectedConfig() != null) {
            String text = excludedMethodForm.methodNamePatternTextField.getText();
            if (!isValidField(text)) {
                validationInfos.add(new ValidationInfo(
                        "Pattern must not contain space character",
                        excludedMethodForm.methodNamePatternTextField
                ));
            }
            if (Objects.equals(text, "")) {
                validationInfos.add(new ValidationInfo(
                        "Pattern must not be empty",
                        excludedMethodForm.methodNamePatternTextField
                ));
            }
            text = excludedMethodForm.classNamePatternTextField.getText();
            if (!isValidField(text)) {
                validationInfos.add(new ValidationInfo(
                        "Pattern must not contain space character",
                        excludedMethodForm.classNamePatternTextField
                ));
            }
            if (Objects.equals(text, "")) {
                validationInfos.add(new ValidationInfo(
                        "Pattern must not be empty",
                        excludedMethodForm.methodNamePatternTextField
                ));
            }
            if (currentTablePanel != null) {
                validationInfos.addAll(DialogHelper.validateParameters(currentTablePanel, currentParameters));
            }
        }
        return validationInfos;
    }

    public static boolean isValidField(String text) {
        return text.indexOf(' ') == -1 &&
                text.indexOf('\n') == -1 &&
                text.indexOf('\t') == -1;
    }

    public void showChooseConfig() {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.EMPTY_CARD_KEY);
    }
}

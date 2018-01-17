package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.IncludedMethodForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class AddIncludingDialogWrapper extends DialogWrapper {
    private final LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
    private final ConfigCheckboxTree tree;
    private final Configuration tempConfiguration;
    private IncludedMethodForm form;
    private JPanel tablePanel = null;

    AddIncludingDialogWrapper(@Nullable Project project, ConfigCheckboxTree tree, Configuration tempConfiguration) {
        super(project);
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
        parameters.add(new MethodConfig.Parameter("*", false));
        init();
        setTitle("Add Included Method Pattern");
        initValidation();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        form = new IncludedMethodForm();
        tablePanel = DialogHelper.createAndShowTable(form.excludedMethodForm.paramTableCards, parameters, tree.treeType);
        form.$$$getRootComponent$$$().setPreferredSize(new Dimension(350, 300));
        return form.$$$getRootComponent$$$();
    }

    @Override
    public boolean isOKActionEnabled() {
        return !Objects.equals(form.excludedMethodForm.classNamePatternTextField.getText(), "") &&
                !Objects.equals(form.excludedMethodForm.methodNamePatternTextField.getText(), "");
    }

    @Override
    protected void doOKAction() {
        /* It may happen that values are not validated */
        if (!MethodFormManager.isValidField(form.excludedMethodForm.classNamePatternTextField.getText()) ||
                Objects.equals(form.excludedMethodForm.classNamePatternTextField.getText(), "") ||
                !MethodFormManager.isValidField(form.excludedMethodForm.methodNamePatternTextField.getText()) ||
                Objects.equals(form.excludedMethodForm.methodNamePatternTextField.getText(), "") ||
                DialogHelper.validateParameters(tablePanel, parameters).size() != 0) {
            super.doOKAction();
            return;
        }
        DialogHelper.saveConfig(form.excludedMethodForm.classNamePatternTextField.getText(),
                form.excludedMethodForm.methodNamePatternTextField.getText(),
                form.saveReturnValueCheckBox.isSelected(),
                parameters,
                tree,
                tempConfiguration
        );

        super.doOKAction();
    }

    @NotNull
    @Override
    protected java.util.List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validationInfos = new LinkedList<>();
        if (!MethodFormManager.isValidField(form.excludedMethodForm.classNamePatternTextField.getText())) {
            validationInfos.add(new ValidationInfo("Pattern must not contain space character",
                    form.excludedMethodForm.classNamePatternTextField));
        }
        if (!MethodFormManager.isValidField(form.excludedMethodForm.methodNamePatternTextField.getText())) {
            validationInfos.add(new ValidationInfo("Pattern must not contain space character",
                    form.excludedMethodForm.methodNamePatternTextField));
        }
        if (tablePanel != null) {
            validationInfos.addAll(DialogHelper.validateParameters(tablePanel, parameters));
        }
        return validationInfos;
    }
}

package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ExcludedMethodForm;
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

public class AddExcludingDialogWrapper extends DialogWrapper {
    private final LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
    private final ConfigCheckboxTree tree;
    private final Configuration tempConfiguration;
    private ExcludedMethodForm form;

    AddExcludingDialogWrapper(@Nullable Project project, ConfigCheckboxTree tree, Configuration tempConfiguration) {
        super(project);
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
        init();
        setTitle("Add Excluded Method Pattern");
        initValidation();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        form = new ExcludedMethodForm();
        DialogHelper.createAndShowTable(form.paramTableCards, parameters, tree.treeType);
        form.$$$getRootComponent$$$().setPreferredSize(new Dimension(350, 300));
        return form.$$$getRootComponent$$$();
    }

    @Override
    protected void doOKAction() {
        DialogHelper.saveConfig(form,
                false,
                parameters,
                tree,
                tempConfiguration
        );

        super.doOKAction();
    }

    @Override
    public boolean isOKActionEnabled() {
        return !Objects.equals(form.classNamePatternTextField.getText(), "") &&
                !Objects.equals(form.methodNamePatternTextField.getText(), "");
    }

    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> validationInfos = new LinkedList<>();
        if (!MethodFormManager.isValidField(form.classNamePatternTextField.getText())) {
            validationInfos.add(new ValidationInfo("Pattern must not contain space character", form.classNamePatternTextField));
        }
        if (!MethodFormManager.isValidField(form.methodNamePatternTextField.getText())) {
            validationInfos.add(new ValidationInfo("Pattern must not contain space character", form.methodNamePatternTextField));
        }
        if (validationInfos.size() == 0) {
            setOKActionEnabled(isOKActionEnabled());
        }
        return validationInfos;
    }
}

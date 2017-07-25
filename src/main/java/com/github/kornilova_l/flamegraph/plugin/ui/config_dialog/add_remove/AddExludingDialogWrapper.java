package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ExcludedMethodForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class AddExludingDialogWrapper extends DialogWrapper {
    private final LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
    private final ConfigCheckboxTree tree;
    private final Configuration tempConfiguration;
    private ExcludedMethodForm form;

    AddExludingDialogWrapper(@Nullable Project project, ConfigCheckboxTree tree, Configuration tempConfiguration) {
        super(project);
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
        init();
        setTitle("Add Excluded Method Pattern");
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
}

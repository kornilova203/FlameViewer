package com.github.kornilova_l.plugin;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.ui.AdjustingTabSettingsEditor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.update.Activatable;
import com.intellij.util.ui.update.UiNotifyConnector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfilerSettingsEditor extends SettingsEditor<RunConfigurationBase> {
    private final RunConfigurationBase configuration;
    private final Project project;

    ProfilerSettingsEditor(RunConfigurationBase configuration) {
        this.configuration = configuration;
        this.project = configuration.getProject();
    }

    @Override
    protected void resetEditorFrom(@NotNull RunConfigurationBase s) {

    }

    @Override
    protected void applyEditorTo(@NotNull RunConfigurationBase s) throws ConfigurationException {

    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        System.out.println("JComponent");
        return new JPanel(new BorderLayout());

    }
}
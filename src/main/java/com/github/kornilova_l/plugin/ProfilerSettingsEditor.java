package com.github.kornilova_l.plugin;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

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
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new Button("Okay"), BorderLayout.SOUTH);
        return panel;
    }
}
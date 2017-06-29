package com.github.kornilova_l.plugin.gui;

import com.github.kornilova_l.plugin.ConfigStorage;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ProfilerSettingsEditor extends SettingsEditor<RunConfigurationBase> {
    private final RunConfigurationBase configuration;
    private final Project project;
    private static ConfigStorage configStorage;

    public ProfilerSettingsEditor(RunConfigurationBase configuration) {
        this.configuration = configuration;
        this.project = configuration.getProject();
        if (configStorage == null) {
            configStorage = (ConfigStorage) this.project.getComponent(PersistentStateComponent.class);
            assert(configStorage.getState() != null);
        }
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
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(new Label("Included Methods"));
        JTextArea textArea = new JTextArea(5, 20);
        textArea.getDocument().addDocumentListener(new ConfigChangeListener());
        panel.add(new JScrollPane(textArea));
        panel.add(new Label("Excluded Methods"));
        JTextArea textArea2 = new JTextArea(5, 20);
        panel.add(new JScrollPane(textArea2));
        return panel;
    }
}
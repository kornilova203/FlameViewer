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
        JTextArea textAreaIncluded = new JTextArea(5, 20);
        assert(configStorage.getState() != null);
        textAreaIncluded.getDocument().addDocumentListener(
                new ConfigChangeListener(configuration, configStorage.getState().includedMethodsMap));
        String text = configStorage.getState().includedMethodsMap.get(configuration.hashCode());
        if (text != null) {
            textAreaIncluded.setText(text);
        }

        panel.add(new JScrollPane(textAreaIncluded));
        panel.add(new Label("Excluded Methods"));
        JTextArea textAreaExcluded = new JTextArea(5, 20);
        textAreaExcluded.getDocument().addDocumentListener(
                new ConfigChangeListener(configuration, configStorage.getState().excludedMethodsMap));
        text = configStorage.getState().includedMethodsMap.get(configuration.hashCode());
        if (text != null) {
            textAreaIncluded.setText(text);
        }
        panel.add(new JScrollPane(textAreaExcluded));
        return panel;
    }
}
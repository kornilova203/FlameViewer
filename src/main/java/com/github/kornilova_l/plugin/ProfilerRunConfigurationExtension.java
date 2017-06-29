package com.github.kornilova_l.plugin;

import com.github.kornilova_l.plugin.gui.ProfilerSettingsEditor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProfilerRunConfigurationExtension extends RunConfigurationExtension {

    ProfilerRunConfigurationExtension() {
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration,
                                                                      JavaParameters params,
                                                                      RunnerSettings runnerSettings) throws ExecutionException {

    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    protected SettingsEditor createEditor(@NotNull RunConfigurationBase configuration) {
        return new ProfilerSettingsEditor(configuration);
    }

    @Nullable
    @Override
    protected String getEditorTitle() {
        return "Profiler";
    }

    @Override
    protected boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return configuration.getType() == ApplicationConfigurationType.getInstance();
    }
}

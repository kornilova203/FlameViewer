package com.github.kornilova_l.plugin;

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
        System.out.println("I am alive!");
    }

    @Nullable
    @Override
    protected SettingsEditor createEditor(@NotNull RunConfigurationBase configuration) {
        System.out.println("createEditor");
        return new ProfilerSettingsEditor(configuration);
//        return new ConfigurationSettingsEditor(configuration);
//        return super.createEditor(configuration);
    }

    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration, JavaParameters params, RunnerSettings runnerSettings) throws ExecutionException {

    }

    @Override
    protected boolean isApplicableFor(@NotNull RunConfigurationBase configuration) {
        return configuration.getType() == ApplicationConfigurationType.getInstance();
    }
}

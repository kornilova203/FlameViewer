package com.github.kornilova_l.flamegraph.plugin.execution;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * ProgramRunner which runs Profiler Executor
 */
public class ProfilerProgramRunner extends DefaultJavaProgramRunner {
    private static final String RUNNER_ID = "ProfileRunnerID";
    Configuration configuration;
    private Project project;

    public ProfilerProgramRunner() {
        super();
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        project = environment.getProject();
        configuration = (Configuration) (environment.getProject().getComponent(PersistentStateComponent.class).getState());
        super.execute(environment);
    }

    @Override
    public void patch(JavaParameters javaParameters,
                      RunnerSettings settings,
                      RunProfile runProfile,
                      boolean beforeExecution) throws ExecutionException {
        assert (configuration != null);
        assert (project != null);
        PluginFileManager fileManager = PluginFileManager.getInstance();
        File configFile = fileManager.getConfigFile(project.getName());
        PluginConfigManager.exportConfig(configFile, configuration);
        String pathToAgent = fileManager.getPathToAgent();
        System.out.println(pathToAgent);
        javaParameters.getVMParametersList().add(
                "-javaagent:" +
                        pathToAgent +
                        "=" +
                        fileManager.getLogDirPath(project.getName()) +
                        "&" +
                        configFile.getAbsolutePath()
        );
    }

    @NotNull
    @Override
    public String getRunnerId() {
        return RUNNER_ID;
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return Objects.equals(executorId, ProfilerExecutor.EXECUTOR_ID);
    }
}

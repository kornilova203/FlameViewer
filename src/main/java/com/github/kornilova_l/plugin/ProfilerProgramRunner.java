package com.github.kornilova_l.plugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.JavaPatchableProgramRunner;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class ProfilerProgramRunner extends JavaPatchableProgramRunner {
    private final String RUNNER_ID = "ProfileRunnerID";

    public ProfilerProgramRunner() {
        System.out.println("Patcher created");
    }

    @Override
    public void patch(JavaParameters javaParameters,
                      RunnerSettings settings,
                      RunProfile runProfile,
                      boolean beforeExecution) throws ExecutionException {
        System.out.println("patch");
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

    /*
    @Override
    public <T extends RunConfigurationBase> void updateJavaParameters(T configuration,
                                                                      JavaParameters params,
                                                                      RunnerSettings runnerSettings) throws ExecutionException {
        ConfigStorage.State state =
                (ConfigStorage.State) configuration.getProject().getComponent(PersistentStateComponent.class).getState();
        params.getVMParametersList().add(
                "-javaagent:/home/lk/java-profiling-plugin/build/libs/javaagent.jar=" +
                        PathManager.getSystemPath()
        );
    }
     */
}

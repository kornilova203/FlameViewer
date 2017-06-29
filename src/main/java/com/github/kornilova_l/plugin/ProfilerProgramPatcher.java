package com.github.kornilova_l.plugin;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.JavaProgramPatcher;

import java.util.Objects;

public class ProfilerProgramPatcher extends JavaProgramPatcher {
    public ProfilerProgramPatcher() {
        System.out.println("Patcher created");
    }

    @Override
    public void patchJavaParameters(Executor executor, RunProfile configuration, JavaParameters javaParameters) {
        if (!Objects.equals(executor.getId(), ProfilerExecutor.EXECUTOR_ID)) {
            System.out.println("I don't like this executor");
            return;
        }
        System.out.println("patchJavaParameters");
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

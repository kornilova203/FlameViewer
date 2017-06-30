package com.github.kornilova_l.plugin.execution;

import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ProgramRunner which runs Profiler Executor
 */
public class ProfilerProgramRunner extends DefaultJavaProgramRunner {
    private static final String RUNNER_ID = "ProfileRunnerID";
    private ConfigStorage.State state;

    public ProfilerProgramRunner() {
        super();
        System.out.println("Patcher created");
    }

    /*
    Get state for this project
     */
    // TODO: find better way to get project component
    @Override
    protected RunContentDescriptor doExecute(@NotNull RunProfileState runProfileState,
                                             @NotNull ExecutionEnvironment env) throws ExecutionException {
        System.out.println("doExecute");
        ConfigStorage configStorage = (ConfigStorage) env.getProject().getComponent(PersistentStateComponent.class);
        state = configStorage.getState();
        return super.doExecute(runProfileState, env);
    }

    @Override
    public void patch(JavaParameters javaParameters,
                      RunnerSettings settings,
                      RunProfile runProfile,
                      boolean beforeExecution) throws ExecutionException {
        System.out.println("patch");
        // TODO: check if string contains spaces
//        String included = state.includedMethodsMap.get(12);
//        String excluded = state.includedMethodsMap.get(12);
//        included = included == null ? "" : included;
//        excluded = excluded == null ? "" : excluded;
        javaParameters.getVMParametersList().add(
                "-javaagent:/home/lk/java-profiling-plugin/build/libs/javaagent.jar=" +
                PathManager.getSystemPath()
//                        + "&" +
//                String.join("&", included.split("\n")) + "&!" +
//                String.join("&!", excluded.split("\n"))
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

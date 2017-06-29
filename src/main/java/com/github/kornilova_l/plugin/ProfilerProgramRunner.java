package com.github.kornilova_l.plugin;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class ProfilerProgramRunner extends DefaultJavaProgramRunner {
    private final String RUNNER_ID = "ProfileRunnerID";

    public ProfilerProgramRunner() {
        super();
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
}

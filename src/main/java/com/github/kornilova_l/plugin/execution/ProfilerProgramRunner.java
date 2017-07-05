package com.github.kornilova_l.plugin.execution;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.MethodConfig;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.*;
import com.intellij.execution.impl.DefaultJavaProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.popup.list.ListPopupImpl;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Objects;

/**
 * ProgramRunner which runs Profiler Executor
 */
public class ProfilerProgramRunner extends DefaultJavaProgramRunner {
    private static final String RUNNER_ID = "ProfileRunnerID";
    private MethodConfig chosenSettings;

    public ProfilerProgramRunner() {
        super();
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        super.execute(environment);
    }

    @Override
    public void patch(JavaParameters javaParameters,
                      RunnerSettings settings,
                      RunProfile runProfile,
                      boolean beforeExecution) throws ExecutionException {
        System.out.println("patch");
        assert (chosenSettings != null);
//        chosenSettings.excluded = chosenSettings.excluded.replaceAll("[ \t]+", "");
//        chosenSettings.included = chosenSettings.included.replaceAll("[ \t]+", "");
        javaParameters.getVMParametersList().add(
                "-javaagent:/home/lk/java-profiling-plugin/build/libs/javaagent.jar=" +
                        PathManager.getSystemPath()
//                        + "&" +
//                        String.join("&", chosenSettings.included.split("\n")) + "&!" +
//                        String.join("&!", chosenSettings.excluded.split("\n"))
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

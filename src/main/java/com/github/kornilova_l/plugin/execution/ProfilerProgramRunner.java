package com.github.kornilova_l.plugin.execution;

import com.github.kornilova_l.plugin.StateContainer;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.github.kornilova_l.plugin.config.ProfilerSettings;
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
import java.util.List;
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

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        state = StateContainer.getState(environment.getProject());
        LinkedList<ProfilerSettings> list = new LinkedList<>();
        list.add(new ProfilerSettings("Slim"));
        list.add(new ProfilerSettings("Shady"));
        new ListPopupImpl(
                new BaseListPopupStep<ProfilerSettings>(
                        "Profiler configuration",
                        new LinkedList<>(state.profilerSettings)
                ) {
                    @Override
                    public PopupStep onChosen(ProfilerSettings selectedValue, boolean finalChoice) {
                        try {
                            DefaultJavaProgramRunner.getInstance().execute(environment);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        return super.onChosen(selectedValue, finalChoice);
                    }
                }
        ).showCenteredInCurrentWindow(environment.getProject());
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

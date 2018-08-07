package com.github.kornilova_l.flamegraph.plugin.execution;

import com.intellij.execution.Executor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static icons.ProfilerIcons.runIcon;

public class ProfilerExecutor extends Executor {
    public static final String EXECUTOR_ID = "Fierix-Executor";

    @Override
    public String getToolWindowId() {
        return ToolWindowId.RUN;
    }

    @Override
    public Icon getToolWindowIcon() {
        return runIcon;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return runIcon;
    }

    @Override
    public Icon getDisabledIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Run selected configuration with profiler enabled";
    }

    @NotNull
    @Override
    public String getActionName() {
        return "Profile";
    }

    @NotNull
    @Override
    public String getId() {
        return EXECUTOR_ID;
    }

    @NotNull
    @Override
    public String getStartActionText() {
        return "Run with Fierix";
    }

    @Override
    public String getStartActionText(String configurationName) {
        final String name = configurationName != null ?
                escapeMnemonicsInConfigurationName(StringUtil.first(configurationName, 30, true)) :
                null;
        return "Run" + (StringUtil.isEmpty(name) ? "" : " '" + name + "'") + " with Fierix";
    }

    private static String escapeMnemonicsInConfigurationName(String configurationName) {
        return configurationName.replace("_", "__");
    }

    @Override
    public String getContextActionId() {
        return "ContextActionId." + getClass();
    }

    @Override
    public String getHelpId() {
        return null;
    }
}

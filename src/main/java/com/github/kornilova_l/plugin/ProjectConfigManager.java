package com.github.kornilova_l.plugin;

import com.github.kornilova_l.config.ConfigStorage;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ProjectConfigManager {
    private static final Map<Project, ConfigStorage.Config> states = new HashMap<>();

    public static ConfigStorage.Config getConfig(@NotNull Project project) {
        return states.computeIfAbsent(
                project,
                k -> ((ConfigStorage) project.getComponent(PersistentStateComponent.class)).getState());
    }
}

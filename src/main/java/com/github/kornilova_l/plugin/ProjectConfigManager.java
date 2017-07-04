package com.github.kornilova_l.plugin;

import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.github.kornilova_l.plugin.config.ConfigStorage.Config;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public class ProjectConfigManager {
    private static final Map<Project, ConfigStorage.Config> states = new HashMap<>();

    public static ConfigStorage.Config getState(Project project) {
        return states.computeIfAbsent(
                project,
                k -> ((ConfigStorage) project.getComponent(PersistentStateComponent.class)).getState());
    }
}

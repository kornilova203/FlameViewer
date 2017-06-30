package com.github.kornilova_l.plugin;

import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.github.kornilova_l.plugin.config.ConfigStorage.State;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;

public class StateContainer {
    private static final Map<Project, State> states = new HashMap<>();

    public static State getState(Project project) {
        State state = states.computeIfAbsent(
                project,
                k -> ((ConfigStorage) project.getComponent(PersistentStateComponent.class)).getState());
        System.out.println(state.configs.keySet());
        return state;
    }
}

package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule;
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class ConfigPanelProvider<B> {

    public abstract void createConfigsGroupingRules(Collection<XConfigGroupingRule> rules);

    public interface ConfigsListener {
        void ConfigsChanged();
    }

    public abstract void addListener(ConfigsListener listener, Project project, Disposable disposable);

    protected abstract void removeListener(ConfigsListener listener);

    public abstract int getPriority();

    @Nullable
    public abstract B findConfig(@NotNull Project project, @NotNull Document document, int offset);

    @Nullable
    public abstract GutterIconRenderer getConfigsGutterIconRenderer(Object config);

    public abstract void onDialogClosed(final Project project);

    public abstract void provideConfigItems(Project project, Collection<ConfigItem> items);
}
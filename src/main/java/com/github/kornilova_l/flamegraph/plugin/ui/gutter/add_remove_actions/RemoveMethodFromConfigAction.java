package com.github.kornilova_l.flamegraph.plugin.ui.gutter.add_remove_actions;

import com.github.kornilova_l.flamegraph.plugin.configuration.ConfigStorage;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.gutter.LineMarkersHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

public class RemoveMethodFromConfigAction extends AddMethodToConfigAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        final Editor editor = getEditor(event, project);
        if (editor == null) {
            return;
        }
        PsiMethod method = getMethod(event, editor, project);
        if (method == null) {
            return;
        }
        assert event.getProject() != null;
        LineMarkersHolder lineMarkersHolder = project.getComponent(LineMarkersHolder.class);
        ConfigStorage.Config config = PluginConfigManager.getConfiguration(event.getProject());
        if (config.isMethodExcluded(method)) {
            return;
        }
        MarkupModelEx markupModel = LineMarkersHolder.getMarkupModel(editor.getDocument(), project);
        if (config.isMethodInstrumented(method)) { // if method is instrumented
            config.maybeRemoveExactIncludingConfig(method);
            if (config.isMethodInstrumented(method)) { // if method is still included
                config.addMethodConfig(method, true);
            }
            lineMarkersHolder.removeIconIfPresent(method, markupModel);
        }
    }
}

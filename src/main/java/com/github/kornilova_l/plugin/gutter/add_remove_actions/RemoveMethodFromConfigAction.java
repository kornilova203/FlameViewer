package com.github.kornilova_l.plugin.gutter.add_remove_actions;

import com.github.kornilova_l.config.ConfigStorage;
import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.gutter.LineMarkersHolder;
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
        ConfigStorage.Config config = ProjectConfigManager.getConfig(event.getProject());
        if (config.isMethodExcluded(method)) {
            return;
        }
        MarkupModelEx markupModel = LineMarkersHolder.getMarkupModel(editor.getDocument(), project);
        if (config.isMethodInstrumented(method)) { // if method is instrumented
            config.maybeRemoveExactIncludingConfig(method);
            if (config.getIncludingConfigs(method).size() != 0) { // if method is still included
                config.addMethod(method, true);
            }
            lineMarkersHolder.removeIconIfPresent(method, markupModel);
        }
    }
}

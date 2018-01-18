package com.github.kornilova_l.flamegraph.plugin.ui.line_markers.add_remove_actions;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.ConfigStorage;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.line_markers.LineMarkersHolder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import static com.github.kornilova_l.flamegraph.configuration.Configuration.getTypes;

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
        PsiMethod psiMethod = getMethod(event, editor, project);
        if (psiMethod == null) {
            return;
        }
        assert event.getProject() != null;
        LineMarkersHolder lineMarkersHolder = project.getComponent(LineMarkersHolder.class);
        Configuration configuration = event.getProject().getComponent(ConfigStorage.class).getState();
        assert configuration != null;
        MethodConfig methodConfig = PluginConfigManager.newMethodConfig(psiMethod);

        if (configuration.isMethodExcluded(methodConfig.getClassPatternString(), methodConfig.getMethodPatternString(),
                getTypes(methodConfig.getParameters()))) {
            return;
        }
        MarkupModelEx markupModel = LineMarkersHolder.getMarkupModel(editor.getDocument(), project);
        lineMarkersHolder.removeInvalidMethods(markupModel);
        if (configuration.isMethodInstrumented(methodConfig)) { // if method is instrumented
            configuration.maybeRemoveExactIncludingConfig(methodConfig);
            if (configuration.isMethodInstrumented(methodConfig)) { // if method is still included
                configuration.addMethodConfig(methodConfig, true);
            }
            lineMarkersHolder.removeIconIfPresent(psiMethod, markupModel);
        }
    }
}

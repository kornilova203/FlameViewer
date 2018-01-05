package com.github.kornilova_l.flamegraph.plugin.ui.line_markers.add_remove_actions;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.ConfigStorage;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.line_markers.LineMarkersHolder;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddMethodToConfigAction extends AnAction {
    @Nullable
    static Editor getEditor(@NotNull AnActionEvent event, @NotNull Project project) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        return editor;
    }

    @Nullable
    static PsiMethod getMethod(@NotNull AnActionEvent event,
                               @NotNull Editor editor,
                               @NotNull Project project) {
        PsiMethod method = null;

        if (ActionPlaces.PROJECT_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.STRUCTURE_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.FAVORITES_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.NAVIGATION_BAR_POPUP.equals(event.getPlace())) {
            final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement instanceof PsiMethod) {
                final PsiFile containingFile = psiElement.getContainingFile();
                if (containingFile != null) {
                    method = (PsiMethod) psiElement;
                }
            }
        } else {
            Document document = editor.getDocument();
            PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if (file != null) {
                final VirtualFile virtualFile = file.getVirtualFile();
                FileType fileType = virtualFile != null ? virtualFile.getFileType() : null;
                if (StdFileTypes.JAVA == fileType || StdFileTypes.CLASS == fileType) {
                    method = findMethod(project, editor);
                }
            }
        }
        return method;
    }

    @Nullable
    private static PsiMethod findMethod(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        final int offset = CharArrayUtil.shiftForward(editor.getDocument().getCharsSequence(), editor.getCaretModel().getOffset(), " \t");
        return DebuggerUtilsEx.findPsiMethod(psiFile, offset);
    }

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
        Configuration configuration = event.getProject().getComponent(ConfigStorage.class).getState();
        assert configuration != null;
        MarkupModelEx markupModel = LineMarkersHolder.getMarkupModel(editor.getDocument(), project);
        MethodConfig methodConfig = PluginConfigManager.newMethodConfig(method);

        configuration.maybeRemoveExactExcludingConfig(methodConfig);
        configuration.addMethodConfig(methodConfig, false);
        if (configuration.isMethodInstrumented(methodConfig) && !lineMarkersHolder.hasIcon(method)) {
            lineMarkersHolder.setIcon(method, markupModel);
        }
    }
}

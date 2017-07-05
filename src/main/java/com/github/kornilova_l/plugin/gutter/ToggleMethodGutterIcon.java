package com.github.kornilova_l.plugin.gutter;

import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
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
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleMethodGutterIcon extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElementAndDocument method = getMethodAndDocument(e);
        if (method == null) {
            return;
        }
        assert e.getProject() != null;
        setIcon(e.getProject(), method);
    }

    private static void setIcon(@NotNull Project project, @NotNull PsiElementAndDocument methodAndDoc) {
        MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(methodAndDoc.getDocument(), project, true);
        RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                methodAndDoc.getPsiElement().getTextOffset(),
                methodAndDoc.getPsiElement().getTextOffset() + 1,
                DebuggerColors.BREAKPOINT_HIGHLIGHTER_LAYER,
                null,
                HighlighterTargetArea.EXACT_RANGE);
        highlighter.setGutterIconRenderer(new ProfilerGutterIconRenderer());
    }

    @Nullable
    private static PsiElementAndDocument getMethodAndDocument(@NotNull AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if(project == null) {
            return null;
        }

        PsiElement method = null;
        Document document = null;

        if (ActionPlaces.PROJECT_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.STRUCTURE_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.FAVORITES_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.NAVIGATION_BAR_POPUP.equals(event.getPlace())) {
            final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
            if(psiElement instanceof PsiMethod) {
                final PsiFile containingFile = psiElement.getContainingFile();
                if (containingFile != null) {
                    method = psiElement;
                    document = PsiDocumentManager.getInstance(project).getDocument(containingFile);
                }
            }
        }
        else {
            Editor editor = event.getData(CommonDataKeys.EDITOR);
            if(editor == null) {
                editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            }
            if (editor != null) {
                document = editor.getDocument();
                PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (file != null) {
                    final VirtualFile virtualFile = file.getVirtualFile();
                    FileType fileType = virtualFile != null ? virtualFile.getFileType() : null;
                    if (StdFileTypes.JAVA == fileType || StdFileTypes.CLASS  == fileType) {
                        method = findMethod(project, editor);
                    }
                }
            }
        }
        if (method == null || document == null) {
            return null;
        }
        return new PsiElementAndDocument(method, document);
    }

    @Nullable
    private static PsiElement findMethod(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if(psiFile == null) {
            return null;
        }
        final int offset = CharArrayUtil.shiftForward(editor.getDocument().getCharsSequence(), editor.getCaretModel().getOffset(), " \t");
        return DebuggerUtilsEx.findPsiMethod(psiFile, offset);
    }
}

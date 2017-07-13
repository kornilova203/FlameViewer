package com.github.kornilova_l.flamegraph.plugin.ui.gutter;

import com.github.kornilova_l.flamegraph.plugin.configuration.ConfigStorage;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class LineMarkersHolder extends AbstractProjectComponent {
    private final HashMap<PsiMethod, RangeHighlighter> rangeHighlighters = new HashMap<>();
    private final ConfigStorage.Config config;

    protected LineMarkersHolder(Project project) {
        super(project);
        config = PluginConfigManager.getConfiguration(myProject);
    }

    public static MarkupModelEx getMarkupModel(Document document, Project project) {
        return (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
    }

    public void setIcon(PsiMethod method, MarkupModelEx markupModel) {
        if (!rangeHighlighters.containsKey(method) || // if no highlighter for this method
                !markupModel.containsHighlighter(rangeHighlighters.get(method))) { // or it isn't shown
            try {
                RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                        method.getTextOffset(),
                        method.getTextOffset() + 1,
                        DebuggerColors.BREAKPOINT_HIGHLIGHTER_LAYER,
                        null,
                        HighlighterTargetArea.EXACT_RANGE);
                highlighter.setGutterIconRenderer(new ProfilerGutterIconRenderer());
                rangeHighlighters.put(method, highlighter);
            } catch (ProcessCanceledException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void removeIconIfPresent(PsiMethod method, MarkupModelEx markupModel) {
        RangeHighlighter highlighter = rangeHighlighters.get(method);
        if (highlighter != null) {
            markupModel.removeHighlighter(highlighter);
        }
    }

    public void updateMethodMarker(@NotNull PsiFile psiFile, @NotNull Document document) {
        new UpdatingPsiElementVisitor(myProject, getMarkupModel(document, myProject))
                .visitElement(psiFile);
    }

    public void updateMethodMarker(PsiMethod psiMethod, MarkupModelEx markupModel) {
        if (config.isMethodInstrumented(psiMethod)) {
            setIcon(psiMethod, markupModel);
        } else {
            removeIconIfPresent(psiMethod, markupModel);
        }
    }

    public void updateMethodMarker(@NotNull VirtualFile file) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(
                myProject,
                file.getName(),
                GlobalSearchScope.fileScope(myProject, file));
        if (psiFiles.length != 1) {
            return;
        }
        PsiFile psiFile = psiFiles[0];
        Document document = psiFile.getViewProvider().getDocument();
        if (document == null) {
            return;
        }
        updateMethodMarker(psiFile, document);
    }
}

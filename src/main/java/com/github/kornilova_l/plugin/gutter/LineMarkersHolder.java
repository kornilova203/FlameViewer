package com.github.kornilova_l.plugin.gutter;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.xdebugger.ui.DebuggerColors;

import java.util.HashMap;

import static com.github.kornilova_l.plugin.config.ConfigStorage.Config.getQualifiedName;

public class LineMarkersHolder extends AbstractProjectComponent {
    private final HashMap<String, RangeHighlighter> rangeHighlighters = new HashMap<>();

    protected LineMarkersHolder(Project project) {
        super(project);
    }

    public void setIcon(PsiMethod method, Project project, Document document) {
        MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        String qualifiedName = getQualifiedName(method);
        if (!rangeHighlighters.containsKey(qualifiedName) || // if no highlighter for this method
                !markupModel.containsHighlighter(rangeHighlighters.get(qualifiedName))) { // or it isn't shown
            RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                    method.getTextOffset(),
                    method.getTextOffset() + 1,
                    DebuggerColors.BREAKPOINT_HIGHLIGHTER_LAYER,
                    null,
                    HighlighterTargetArea.EXACT_RANGE);
            highlighter.setGutterIconRenderer(new ProfilerGutterIconRenderer());
            rangeHighlighters.put(getQualifiedName(method), highlighter);
        }
    }

    public void removeIcon(PsiMethod method, Project project, Document document) {
            RangeHighlighter highlighter = rangeHighlighters.get(getQualifiedName(method));
            MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
            markupModel.removeHighlighter(highlighter);
    }
}

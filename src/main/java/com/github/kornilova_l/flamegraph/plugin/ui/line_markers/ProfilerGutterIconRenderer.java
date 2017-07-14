package com.github.kornilova_l.flamegraph.plugin.ui.line_markers;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;

public class ProfilerGutterIconRenderer extends GutterIconRenderer {
    private static final Icon methodIcon = IconLoader.getIcon("/icons/flame16.png",
            ProfilerGutterIconRenderer.class);
    private final DefaultActionGroup actionGroup = new DefaultActionGroup();

    ProfilerGutterIconRenderer(Collection<MethodConfig> includingMethodConfigs) {
        initActionGroup(includingMethodConfigs);
    }

    private void initActionGroup(Collection<MethodConfig> includingMethodConfigs) {
        for (MethodConfig includingMethodConfig : includingMethodConfigs) {
            actionGroup.add(new AnAction() {
                @Override
                public void actionPerformed(AnActionEvent e) {

                }

                @Override
                public void update(AnActionEvent e) {
                    Presentation presentation = e.getPresentation();
                    presentation.setText(includingMethodConfig.toString());
                    super.update(e);
                }
            });
        }
    }

    @Nullable
    @Override
    public AnAction getClickAction() {
        return new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                if (e.getInputEvent() instanceof MouseEvent) {
                    MouseEvent mouseEvent = ((MouseEvent) e.getInputEvent());
                    final DataContext context = SimpleDataContext.getProjectContext(null);
                    JBPopup popup = JBPopupFactory.getInstance()
                            .createActionGroupPopup("Edit Method Patterns",
                                    actionGroup,
                                    context,
                                    JBPopupFactory.ActionSelectionAid.MNEMONICS,
                                    true);
                    popup.show(new RelativePoint(mouseEvent));
                }
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProfilerGutterIconRenderer;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return methodIcon;
    }
}

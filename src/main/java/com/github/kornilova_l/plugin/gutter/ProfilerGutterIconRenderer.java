package com.github.kornilova_l.plugin.gutter;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProfilerGutterIconRenderer extends GutterIconRenderer {
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
        return AllIcons.General.ArrowDown;
    }
}

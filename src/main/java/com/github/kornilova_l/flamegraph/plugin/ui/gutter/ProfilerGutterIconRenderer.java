package com.github.kornilova_l.flamegraph.plugin.ui.gutter;

import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ProfilerGutterIconRenderer extends GutterIconRenderer {
    private static final Icon methodIcon = IconLoader.getIcon("/icons/flame16.png",
            ProfilerGutterIconRenderer.class);

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

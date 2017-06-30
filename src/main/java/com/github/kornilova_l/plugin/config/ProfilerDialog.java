package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ProfilerDialog extends DialogWrapper {
    protected ProfilerDialog(@Nullable Project project) {
        super(project);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(new Label("Included Methods"));

        return panel;
    }
}

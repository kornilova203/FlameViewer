package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.config.MethodConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class DetailViewManager {
    static JComponent getDetailView(@NotNull MethodConfig config) {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(new Label(config.getQualifiedName()));
        return panel;
    }

}
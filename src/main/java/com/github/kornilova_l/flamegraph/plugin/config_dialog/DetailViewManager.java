package com.github.kornilova_l.flamegraph.plugin.config_dialog;

import com.github.kornilova_l.flamegraph.config.MethodConfigOld;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class DetailViewManager {
    static JComponent getDetailView(@NotNull MethodConfigOld config) {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(new Label(config.getQualifiedName()));
        return panel;
    }

}
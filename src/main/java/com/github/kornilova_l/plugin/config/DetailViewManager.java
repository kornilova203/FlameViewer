package com.github.kornilova_l.plugin.config;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class DetailViewManager {
    static JComponent getDetailView(@NotNull Config config) {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(new Label(config.name));
        return panel;
    }

}
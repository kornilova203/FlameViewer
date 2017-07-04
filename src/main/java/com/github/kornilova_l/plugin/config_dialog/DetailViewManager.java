package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.config.ConfigNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

class DetailViewManager {
    static JComponent getDetailView(@NotNull ConfigNode config) {
        JPanel panel = new JPanel(new GridLayout());
        panel.add(new Label(config.name));
        return panel;
    }

}
package com.github.kornilova_l.plugin.config;

import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;

public class AddConfigAction implements AnActionButtonRunnable {
    @Override
    public void run(AnActionButton anActionButton) {
        System.out.println("run");
    }
}

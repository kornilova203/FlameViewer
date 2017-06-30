package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ProfilerDialog extends DialogWrapper {
    protected ProfilerDialog(@Nullable Project project) {
        super(project);
        init();
        setTitle("MyTitle");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        Splitter splitter = new Splitter(false, 0.3f);
        panel.add(splitter, BorderLayout.CENTER);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(new ConfigList());
        toolbarDecorator.setAddAction(new AddConfigAction());
        toolbarDecorator.setRemoveAction(new AddConfigAction());
        splitter.setFirstComponent(toolbarDecorator.createPanel());

        return panel;
    }
}

package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.util.ui.ListItemsDialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class ProfilerDialog extends ListItemsDialogWrapper {
    private final ConfigStorage.State state;
    private JPanel secondComponent = new JPanel(new GridLayout(1, 1));

    protected ProfilerDialog(Project project) {
        super("MyTitle");
        ConfigStorage configStorage = (ConfigStorage) project.getComponent(PersistentStateComponent.class);
        state = configStorage.getState();
        myData = new LinkedList<>();
        myList.addListSelectionListener(e -> {
            int index = myList.getSelectedIndex();
            if (index == -1) {
                return;
            }
            secondComponent.add(new Label("Hello"));
            System.out.println(myList.getSelectedIndex());
        });
    }

    @Override
    protected String createAddItemDialog() {
        return Messages.showInputDialog(
                "Enter configuration name:",
                "Create New Configuration",
                Messages.getQuestionIcon());
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        Splitter splitter = new Splitter(false, 0.3f);
        panel.add(splitter, BorderLayout.CENTER);

        splitter.setFirstComponent(myPanel);
        secondComponent = new JPanel(new GridLayout(1, 1));
        secondComponent.add(new Label("Hello"));
        splitter.setSecondComponent(secondComponent);

        return panel;
    }
}

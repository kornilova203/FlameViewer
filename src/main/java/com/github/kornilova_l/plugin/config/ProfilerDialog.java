package com.github.kornilova_l.plugin.config;

import com.github.kornilova_l.plugin.StateContainer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.util.ui.ListItemsDialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class ProfilerDialog extends ListItemsDialogWrapper {
    private final ConfigStorage.State state;
    private Splitter splitter;

    protected ProfilerDialog(Project project) {
        super("Edit Profiler Configurations");
        System.out.println("dialog created");
        state = StateContainer.getState(project);
        assert(state != null);
//        myData = new LinkedList<>(state.configs.keySet());
        setData(new LinkedList<>(state.configs.keySet()));
        System.out.println(myData);
        myList.addListSelectionListener(e -> {
            int index = myList.getSelectedIndex();
            if (index == -1) {
                return;
            }
            splitter.setSecondComponent(getSecondComponent(index));
        });
    }

    private JComponent getSecondComponent(int index) {
        JComponent secondComponent = new JPanel(new GridLayout(1, 2));
        secondComponent.add(new Label("hello, " + myData.get(index)));
        return secondComponent;
    }

    @Override
    protected String createAddItemDialog() {
        String configName = Messages.showInputDialog(
                "Enter configuration name:",
                "Create New Configuration",
                Messages.getQuestionIcon());
        state.configs.putIfAbsent(configName, new ConfigStorage.State.Config());
        return configName;
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        Splitter splitter = new Splitter(false, 0.3f);
        panel.add(splitter, BorderLayout.CENTER);

        splitter.setFirstComponent(myPanel);
        JComponent secondComponent = new JPanel(new GridLayout(1, 1));
        secondComponent.add(new Label("Hello"));
        splitter.setSecondComponent(secondComponent);

        this.splitter = splitter;
        return panel;
    }
}

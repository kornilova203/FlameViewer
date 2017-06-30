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
        state = StateContainer.getState(project);
        assert(state != null);
        setData(new LinkedList<>(state.configs.keySet()));
        myList.addListSelectionListener(e -> {
            int index = myList.getSelectedIndex();
            if (index == -1) {
                splitter.setSecondComponent(new JPanel(new GridLayout(1, 1)));
                return;
            }
            splitter.setSecondComponent(getSecondComponent(index));
        });
        if (myData.size() > 0) {
            splitter.setSecondComponent(getSecondComponent(0));
        }
    }

    private JComponent getSecondComponent(int index) {
        JComponent secondComponent = new JPanel(new GridLayout(4, 1));
        secondComponent.add(new Label("Included methods:"));
        JTextArea textArea = new JTextArea(state.configs.get(myData.get(index)).included);
        textArea.getDocument().addDocumentListener(new ConfigChangeListener(state.configs.get(myData.get(index)), true));
        secondComponent.add(new JScrollPane(
                textArea
        ));
        secondComponent.add(new Label("Excluded methods:"));
        JTextArea textArea2 = new JTextArea(state.configs.get(myData.get(index)).excluded);
        textArea2.getDocument().addDocumentListener(new ConfigChangeListener(state.configs.get(myData.get(index)), false));
        secondComponent.add(new JScrollPane(
                textArea2
        ));
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
        splitter.setSecondComponent(new JPanel(new GridLayout(1, 1)));

        this.splitter = splitter;
        return panel;
    }
}

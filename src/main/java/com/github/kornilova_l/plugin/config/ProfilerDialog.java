package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.ui.Splitter;
import com.intellij.util.ui.ListItemsDialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class ProfilerDialog extends ListItemsDialogWrapper {
    private static int i = 0;
    private JPanel secondComponent = new JPanel(new GridLayout(1, 1));
    private static final LinkedList<String> message = new LinkedList<>();

    static {
        message.addFirst("Please stand up, please stand up?");
        message.addFirst("So won't the real Slim Shady please stand up");
        message.addFirst("All you other Slim Shadys are just imitating");
        message.addFirst("I'm Slim Shady, yes, I'm the real Shady");
    }

    protected ProfilerDialog() {
        super("MyTitle");
        myData = new LinkedList<>();
        myList.addListSelectionListener(e -> {
            int index = myList.getSelectedIndex();
            if (index == -1) {
                return;
            }
            secondComponent.add(new Label("Hello"));
            System.out.println("selected");
            System.out.println(myList.getSelectedIndex());
        });
    }

    @Override
    protected String createAddItemDialog() {
        if (i < 4) {
            return message.get(i++);
        }
        return message.get(3);
//        return Messages.showInputDialog(ApplicationBundle.message("editbox.enter.tag.name"),
//                ApplicationBundle.message("title.tag.name"), Messages.getQuestionIcon());
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

package com.github.kornilova_l.plugin.config;

import com.intellij.util.ui.ListItemsDialogWrapper;

import java.util.LinkedList;

public class ProfilerDialog extends ListItemsDialogWrapper {
    private static int i = 0;
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
}

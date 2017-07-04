package com.github.kornilova_l.plugin.config;

import com.intellij.codeInsight.intention.impl.config.TextDescriptor;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class DetailViewPanel implements Disposable {
    private JPanel panel = new JPanel(new GridLayout(1, 1));

    private JPanel afterPanel;
    private JPanel beforePanel;
    private final JEditorPane myDescriptionBrowser = new JEditorPane();

    public DetailViewPanel() {
        myDescriptionBrowser.setMargin(new Insets(5, 5, 5, 5));
//        initializeExamplePanel(afterPanel);
//        initializeExamplePanel(beforePanel);
    }


    public void reset(@NotNull Config config) {
        myDescriptionBrowser.setText("hello");
        panel.add(new JLabel("label"));


//        showUsages(beforePanel, new PlainTextDescriptor(CodeInsightBundle.message("templates.postfix.settings.category.before"),
//                "before.txt.template")
//                : ArrayUtil.getFirstElement(config.getExampleUsagesBefore()));
//        showUsages(afterPanel, isEmpty
//                ? new PlainTextDescriptor(CodeInsightBundle.message("templates.postfix.settings.category.after"),
//                "after.txt.template")
//                : ArrayUtil.getFirstElement(config.getExampleUsagesAfter()));
    }

    private static void showUsages(@NotNull JPanel panel, @Nullable TextDescriptor exampleUsage) {
//        String text = "";
//        FileType fileType = PlainTextFileType.INSTANCE;
//        if (exampleUsage != null) {
//            try {
//                text = exampleUsage.getText();
//                String name = exampleUsage.getFileName();
//                FileTypeManagerEx fileTypeManager = FileTypeManagerEx.getInstanceEx();
//                String extension = fileTypeManager.getExtension(name);
//                fileType = fileTypeManager.getFileTypeByExtension(extension);
//            }
//            catch (IOException e) {
//                LOG.error(e);
//            }
//        }
//
//        ((ActionUsagePanel)panel.getComponent(0)).reset(text, fileType);
//        panel.repaint();
    }

    private void initializeExamplePanel(@NotNull JPanel panel) {
        System.out.println("initializeExamplePanel");
//        panel.setLayout(new BorderLayout());
//        ActionUsagePanel actionUsagePanel = new ActionUsagePanel();
//        panel.add(actionUsagePanel);
//        Disposer.register(this, actionUsagePanel);
    }

    synchronized JPanel getComponent() {
        return panel;
    }

    @Override
    public void dispose() {
    }
}
package com.github.kornilova_l.plugin.config_dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AddMethod extends DialogWrapper {
    private JPanel panel;
    private JTextField classPatternTextField;
    private JTextField methodNameTextField;

    protected AddMethod(@Nullable Project project) {
        super(project);
        setTitle("Add Method");
        init();
    }

    protected void doOKAction() {
        if (getClassPattern().length() == 0) {
            Messages.showErrorDialog(panel, "Class pattern not specified");
            return;
        }
        if (getMethodName().length() == 0) {
            Messages.showErrorDialog(panel, "Method name not specified");
            return;
        }
        super.doOKAction();
    }

    public String getClassPattern() {
        return classPatternTextField.getText().trim();
    }

    public String getMethodName() {
        return methodNameTextField.getText().trim();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }
}

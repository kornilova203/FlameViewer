package com.github.kornilova_l.flamegraph.plugin.config_dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class AddMethod extends DialogWrapper {
    private JPanel panel;
    private JTextField classPatternTextField;
    private JTextField methodPatternTextField;
    private JTextField parametersPatternTextField;
    private JTextPane classDescription;
    private JTextPane methodDescription;
    private JTextPane parametersDescription;

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
        if (getMethodPattern().length() == 0) {
            Messages.showErrorDialog(panel, "Method name not specified");
            return;
        }
        super.doOKAction();
    }

    public String getClassPattern() {
        return classPatternTextField.getText().trim();
    }

    public String getParametersPattern() {
        String parameters = parametersPatternTextField.getText().trim();
        return Objects.equals(parameters, "") ?
                "(*)" :
                parameters;
    }

    public String getMethodPattern() {
        return methodPatternTextField.getText().trim();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ExcludedMethodForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MyTableView;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;


class DialogHelper {
    private static final String cardKey = "new-table";

    static void createAndShowTable(JPanel paramTableCards, List<MethodConfig.Parameter> parameters, ConfigCheckboxTree.TreeType treeType) {
        paramTableCards.add(MyTableView.createTablePanel(parameters, treeType), cardKey);
        ((CardLayout) paramTableCards.getLayout()).show(paramTableCards, cardKey);
    }

    static void saveConfig(ExcludedMethodForm excludedMethodForm, boolean saveRetVal, LinkedList<MethodConfig.Parameter> parameters, ConfigCheckboxTree tree, Configuration tempConfig) {
        MethodConfig methodConfig = new MethodConfig(
                excludedMethodForm.classNamePatternTextField.getText(),
                excludedMethodForm.methodNamePatternTextField.getText(),
                parameters,
                true,
                saveRetVal
        );
        switch (tree.treeType) {
            case INCLUDING:
                tempConfig.addMethodConfig(methodConfig, false);
                break;
            case EXCLUDING:
                tempConfig.addMethodConfig(methodConfig, true);
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
        tree.addNode(methodConfig);
    }
}

package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.export_import;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ShowMessage extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;

    public ShowMessage() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    public static void main(String[] args) {
        ShowMessage dialog = new ShowMessage();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}

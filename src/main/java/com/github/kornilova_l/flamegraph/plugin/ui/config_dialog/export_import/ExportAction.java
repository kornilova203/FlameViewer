package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.export_import;

import com.github.kornilova_l.flamegraph.configuration.Configuration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExportAction extends AbstractAction {
    private Configuration configuration;

    public ExportAction(Configuration configuration) {
        this.configuration = configuration;
        this.putValue("Name", "Export");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(new JPanel()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            exportToFile(file);
        }
    }

    private void exportToFile(File file) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(configuration.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

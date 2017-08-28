package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.export_import;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ExportAction extends AbstractAction {
    private Configuration configuration;
    private Project project;

    public ExportAction(Configuration configuration, Project project) {
        this.configuration = configuration;
        this.project = project;
        this.putValue("Name", "Export");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(
                new FileSaverDescriptor("Export Configuration", "Export to"),
                project
        );
        VirtualFileWrapper targetFile = dialog.save(project.getBaseDir(), project.getName() + "-profiler.config");
        if (targetFile != null) {
            exportToFile(targetFile.getFile());
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

package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.export_import;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ChangeConfigurationDialog;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImportAction extends AbstractAction {
    private Configuration configuration;
    private final ChangeConfigurationDialog dialog;

    public ImportAction(Configuration configuration, ChangeConfigurationDialog dialog) {
        this.configuration = configuration;
        this.dialog = dialog;
        this.putValue("Name", "Import");
    }

    private void importConfig(VirtualFile file) {
        try {
            byte[] bytes = file.contentsToByteArray();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            if (Configuration.isValid(inputStream)) {
                configuration.assign(new Configuration(new ByteArrayInputStream(bytes)));
                dialog.getIncludedTree().initTree(configuration.getIncludingMethodConfigs());
                dialog.getExcludedTree().initTree(configuration.getExcludingMethodConfigs());
            } else {
                // todo
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooser.chooseFile(
                new FileChooserDescriptor(true, false, false, false, false, false),
                null,
                null,
                this::importConfig
        );
    }
}

package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class ConfigsTreeCellRenderer  {
    private static final SimpleTextAttributes SIMPLE_CELL_ATTRIBUTES_BOLD = SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_BOLD, null, null, null);

    private static void customizeRenderer(Project project,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          ColoredTreeCellRenderer renderer) {
        if (value instanceof ConfigItemNode) {
            ConfigItemNode node = (ConfigItemNode)value;
            ConfigItem config = node.getConfigItem();
            config.setupRenderer(renderer, project, selected);
        }
        else if (value instanceof ConfigsGroupNode) {
            XConfigGroup group = ((ConfigsGroupNode)value).getGroup();
            renderer.setIcon(group.getIcon(expanded));
            if (group instanceof XConfigCustomGroup && ((XConfigCustomGroup)group).isDefault()) {
                renderer.append(group.getName(), SIMPLE_CELL_ATTRIBUTES_BOLD);
            }
            else {
                renderer.append(group.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
            }
        }
    }

    public static class ConfigsCheckboxTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {
        private final Project myProject;

        public ConfigsCheckboxTreeCellRenderer(Project project) {
            myProject = project;
        }

        @Override
        public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ConfigsTreeCellRenderer.customizeRenderer(myProject, value, selected, expanded, getTextRenderer());
        }
    }

    public static class ConfigsSimpleTreeCellRenderer extends ColoredTreeCellRenderer {
        private final Project myProject;

        public ConfigsSimpleTreeCellRenderer(Project project) {
            myProject = project;
        }

        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            customizeRenderer(myProject, value, selected, expanded, this);
        }
    }
}

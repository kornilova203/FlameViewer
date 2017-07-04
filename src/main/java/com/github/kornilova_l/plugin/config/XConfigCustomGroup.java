package com.github.kornilova_l.plugin.config;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class XConfigCustomGroup extends XConfigGroup {
    private final String myName;
    private final boolean myIsDefault;

    public XConfigCustomGroup(@NotNull String name, Project project) {
        myName = name;
        myIsDefault = name.equals(((XBreakpointManagerImpl) XDebuggerManager.getInstance(project).getBreakpointManager()).getDefaultGroup());
    }

    @Nullable
    public Icon getIcon(final boolean isOpen) {
        return AllIcons.Nodes.NewFolder;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    public boolean isDefault() {
        return myIsDefault;
    }
}

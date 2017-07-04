package com.github.kornilova_l.plugin.config;

import com.intellij.xdebugger.breakpoints.ui.XBreakpointsGroupingPriorities;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Comparator;

public abstract class XConfigGroupingRule<B, G extends XConfigGroup> {
    public static final Comparator<XConfigGroupingRule> PRIORITY_COMPARATOR = (o1, o2) -> {
        final int res = o2.getPriority() - o1.getPriority();
        return res != 0 ? res : (o1.getId().compareTo(o2.getId()));
    };

    private final String myId;
    private final String myPresentableName;

    public boolean isAlwaysEnabled() {
        return false;
    }

    protected XConfigGroupingRule(final @NotNull @NonNls String id, final @NonNls @Nls String presentableName) {
        myId = id;
        myPresentableName = presentableName;
    }

    @NotNull
    public String getPresentableName() {
        return myPresentableName;
    }

    @NotNull
    public String getId() {
        return myId;
    }

    public int getPriority() {
        return XBreakpointsGroupingPriorities.DEFAULT;
    }

    @Nullable
    public abstract G getGroup(@NotNull B breakpoint, @NotNull Collection<G> groups);

    @Nullable
    public Icon getIcon() {
        return null;
    }
}

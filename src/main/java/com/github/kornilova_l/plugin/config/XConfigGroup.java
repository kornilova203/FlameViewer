package com.github.kornilova_l.plugin.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class XConfigGroup implements Comparable<XConfigGroup> {
    @Nullable
    public Icon getIcon(boolean isOpen) {
        return null;
    }

    @NotNull
    public abstract String getName();

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        return (getClass() == obj.getClass()) && compareTo((XConfigGroup) obj) == 0;
    }

    public int compareTo(@NotNull final XConfigGroup o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
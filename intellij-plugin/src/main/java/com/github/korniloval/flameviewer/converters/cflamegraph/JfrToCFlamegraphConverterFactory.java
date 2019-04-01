package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class JfrToCFlamegraphConverterFactory implements ToCFlamegraphConverterFactory {

    @Nullable
    @Override
    public ToCFlamegraphConverter create(@NotNull File file) {
        if (!isSupported(file)) return null;
        return new JfrConvertersComposition(file);
    }

    private boolean isSupported(@NotNull File file) {
        return StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.getName()), "jfr");
    }
}

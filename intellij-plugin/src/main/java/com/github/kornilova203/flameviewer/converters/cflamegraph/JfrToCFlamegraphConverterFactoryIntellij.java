package com.github.kornilova203.flameviewer.converters.cflamegraph;

import com.github.kornilova203.flameviewer.converters.Converter;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import static com.github.kornilova203.flameviewer.converters.cflamegraph.JfrToStacksConverter.EXTENSION;

public class JfrToCFlamegraphConverterFactoryIntellij implements ToCFlamegraphConverterFactory {

    @Nullable
    @Override
    public Converter<? extends CFlamegraph> create(@NotNull File file) {
        return isSupported(file) ? new JfrConvertersComposition(file) : null;
    }

    @Override
    public boolean isSupported(@NotNull File file) {
        return StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.getName()), EXTENSION);
    }
}

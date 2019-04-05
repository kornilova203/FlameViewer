package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.github.korniloval.flameviewer.converters.Converter;
import com.github.korniloval.flameviewer.converters.ConverterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ToCFlamegraphConverterFactory extends ConverterFactory<CFlamegraph> {
    @Nullable
    @Override
    Converter<CFlamegraph> create(@NotNull File file);
}

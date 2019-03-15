package com.github.korniloval.flameviewer.converters.flamegraph.jfr;

import com.github.korniloval.flameviewer.converters.flamegraph.ProfilerToFlamegraphConverter;
import com.github.korniloval.flameviewer.converters.flamegraph.ProfilerToFlamegraphConverterFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @deprecated should be replaced with implementation of ProfilerToCFlamegraphConverterFactory
 */
class JfrToFlamegraphConverterFactory implements ProfilerToFlamegraphConverterFactory {

    @Override
    public boolean isSupported(@NotNull File file) {
        return StringUtil.equalsIgnoreCase(PathUtil.getFileExtension(file.getName()), "jfr");
    }

    @NotNull
    @Override
    public ProfilerToFlamegraphConverter create(@NotNull File file) {
        return new JfrToFlamegraphConverter(file);
    }
}
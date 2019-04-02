package com.github.korniloval.flameviewer.converters.calltraces;

import com.github.korniloval.flameviewer.FlameLogger;
import com.github.korniloval.flameviewer.LoggerAdapter;
import com.github.korniloval.flameviewer.converters.calltraces.flamegraph.FlamegraphToCallTracesConverter;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.github.korniloval.flameviewer.converters.ConvertersUtilKt.getBytes;
import static com.github.korniloval.flameviewer.converters.calltraces.flamegraph.StacksParser.isFlamegraph;

/**
 * Converts file in flamegraph format to call traces.
 *
 * This format does not allow unmerged stacktraces.
 * So if a file has following content:
 * a() 5
 * a() 5
 * Second line will be ignored.
 */
public class FlamegraphToCallTracesConverterFactory implements ToCallTracesIdentifiedConverterFactory {
    private static final FlameLogger logger = new LoggerAdapter(Logger.getInstance(FlamegraphToCallTracesConverterFactory.class));
    private static final String EXTENSION = "flamegraph";

    @NotNull
    public String getId() {
        return EXTENSION;
    }

    public boolean isSupported(@NotNull File file) {
        byte[] bytes = getBytes(file, logger);
        if (bytes == null) return false;
        return isFlamegraph(bytes);
    }

    @NotNull
    @Override
    public ToCallTracesConverter create(@NotNull File file) {
        return new FlamegraphToCallTracesConverter(file);
    }
}

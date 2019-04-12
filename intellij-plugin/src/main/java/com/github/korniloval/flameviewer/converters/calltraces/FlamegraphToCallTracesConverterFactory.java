package com.github.korniloval.flameviewer.converters.calltraces;

import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.korniloval.flameviewer.FlameLogger;
import com.github.korniloval.flameviewer.LoggerAdapter;
import com.github.korniloval.flameviewer.converters.Converter;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.github.korniloval.flameviewer.converters.calltraces.FlamegraphToCallTracesConverter.EXTENSION;
import static com.github.korniloval.flameviewer.converters.calltraces.StacksParser.isFlamegraph;

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

    @NotNull
    public String getId() {
        return EXTENSION;
    }

    @Override
    public boolean isSupported(@NotNull File file) {
        return isFlamegraph(file, logger);
    }

    @NotNull
    @Override
    public Converter<TreeProtos.Tree> create(@NotNull File file) {
        return new FlamegraphToCallTracesConverter(file);
    }
}

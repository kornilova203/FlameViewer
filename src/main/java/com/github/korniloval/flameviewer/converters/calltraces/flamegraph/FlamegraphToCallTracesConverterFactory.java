package com.github.korniloval.flameviewer.converters.calltraces.flamegraph;

import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverter;
import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverterFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.github.korniloval.flameviewer.converters.calltraces.flamegraph.StacksParser.isFlamegraph;
import static com.github.korniloval.flameviewer.converters.cflamegraph.jfr.JfrToCFlamegraphConverterFactory.getBytes;

/**
 * Converts file in flamegraph format to call traces.
 *
 * This format does not allow unmerged stacktraces.
 * So if a file has following content:
 * a() 5
 * a() 5
 * Second line will be ignored.
 */
public class FlamegraphToCallTracesConverterFactory implements FileToCallTracesConverterFactory {

    private static final String EXTENSION = "flamegraph";

    @NotNull
    public String getId() {
        return EXTENSION;
    }

    public boolean isSupported(@NotNull File file) {
        return isFlamegraph(getBytes(file));
    }

    @NotNull
    @Override
    public FileToCallTracesConverter create(@NotNull File file) {
        return new FlamegraphToCallTracesConverter(file);
    }
}

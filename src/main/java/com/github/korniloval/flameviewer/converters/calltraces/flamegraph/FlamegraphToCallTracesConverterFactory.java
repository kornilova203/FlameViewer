package com.github.korniloval.flameviewer.converters.calltraces.flamegraph;

import com.github.korniloval.flameviewer.converters.UtilKt;
import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverter;
import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverterFactory;
import com.intellij.openapi.diagnostic.Logger;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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
public class FlamegraphToCallTracesConverterFactory implements FileToCallTracesConverterFactory {
    private static final Logger LOG = Logger.getInstance(FlamegraphToCallTracesConverterFactory.class);
    private static final String EXTENSION = "flamegraph";

    @NotNull
    public String getId() {
        return EXTENSION;
    }

    public boolean isSupported(@NotNull File file) {
        byte[] bytes = UtilKt.getBytes(file, e -> { LOG.error(e); return Unit.INSTANCE; });
        if (bytes == null) return false;
        return isFlamegraph(bytes);
    }

    @NotNull
    @Override
    public FileToCallTracesConverter create(@NotNull File file) {
        return new FlamegraphToCallTracesConverter(file);
    }
}

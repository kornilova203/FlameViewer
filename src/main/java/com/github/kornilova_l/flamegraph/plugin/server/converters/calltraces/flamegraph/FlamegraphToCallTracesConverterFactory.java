package com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.flamegraph;

import com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.FileToCallTracesConverter;
import com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.FileToCallTracesConverterFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.flamegraph.StacksParser.isFlamegraph;
import static com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.jfr.JfrToCFlamegraphConverterFactory.getBytes;

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

    @NotNull
    public String getId() {
        return "flamegraph";
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

package com.github.kornilova203.flameviewer.converters.calltraces;

import com.github.kornilova203.flameviewer.FlameLogger;
import com.github.kornilova203.flameviewer.LoggerAdapter;
import com.github.kornilova203.flameviewer.converters.Converter;
import com.github.kornilova203.flameviewer.converters.cflamegraph.FlamegraphToCFlamegraphConverter;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.github.kornilova203.flameviewer.converters.ConvertersUtilKt.isFlamegraph;
import static com.github.kornilova203.flameviewer.converters.calltraces.FlamegraphToCallTracesConverter.EXTENSION;

/**
 * Converts file in flamegraph format to call traces.
 *
 * This format does not allow unmerged stacktraces.
 * So if a file has following content:
 * a() 5
 * a() 5
 * Second line will be ignored.
 *
 * @deprecated this converter supports flamegraph files that were uploaded before {@link FlamegraphToCFlamegraphConverter}
 *             was implemented
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

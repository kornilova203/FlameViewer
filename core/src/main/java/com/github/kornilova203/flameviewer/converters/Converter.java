package com.github.kornilova203.flameviewer.converters;

import com.github.kornilova203.flameviewer.FlameIndicator;
import org.jetbrains.annotations.Nullable;

/**
 * @author Liudmila Kornilova
 **/
public interface Converter<T> {
    T convert(@Nullable FlameIndicator indicator) throws ConversionException;
}

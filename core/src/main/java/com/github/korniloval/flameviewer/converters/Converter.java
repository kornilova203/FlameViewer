package com.github.korniloval.flameviewer.converters;

import com.github.korniloval.flameviewer.FlameIndicator;
import org.jetbrains.annotations.Nullable;

/**
 * @author Liudmila Kornilova
 **/
public interface Converter<T> {
    T convert(@Nullable FlameIndicator indicator) throws ConversionException;
}

package com.github.korniloval.flameviewer.converters;

/**
 * @author Liudmila Kornilova
 **/
public interface Converter<T> {
    T convert() throws ConversionException;
}

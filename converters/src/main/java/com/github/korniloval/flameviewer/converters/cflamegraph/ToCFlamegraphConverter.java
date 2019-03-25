package com.github.korniloval.flameviewer.converters.cflamegraph;

import com.github.korniloval.flameviewer.converters.ConversionException;
import com.github.korniloval.flameviewer.converters.Converter;

/**
 * @author Liudmila Kornilova
 **/
public interface ToCFlamegraphConverter extends Converter<CFlamegraph> {
    @Override
    CFlamegraph convert() throws ConversionException;
}

package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.ConversionException
import com.github.korniloval.flameviewer.converters.Converter

/**
 * @author Liudmila Kornilova
 **/
interface ToCallTracesConverter : Converter<TreeProtos.Tree> {
    @Throws(ConversionException::class)
    override fun convert(): TreeProtos.Tree
}
package com.github.korniloval.flameviewer.converters.cflamegraph

import com.github.korniloval.flameviewer.converters.Converter

interface ProfilerToCFlamegraphConverter : Converter<CFlamegraph> {
    override fun convert(): CFlamegraph
}

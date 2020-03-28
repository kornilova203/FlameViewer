package com.github.kornilova203.flameviewer.converters.calltraces

import com.github.kornilova203.flameviewer.converters.IdentifiedConverterFactory
import com.github.kornilova_l.flamegraph.proto.TreeProtos

interface ToCallTracesIdentifiedConverterFactory : IdentifiedConverterFactory<TreeProtos.Tree>

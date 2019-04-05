package com.github.korniloval.flameviewer.converters.calltraces

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.korniloval.flameviewer.converters.IdentifiedConverterFactory

interface ToCallTracesIdentifiedConverterFactory : IdentifiedConverterFactory<TreeProtos.Tree>

package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.converters.trees.TreeType.CALL_TRACES
import com.github.kornilova203.flameviewer.server.ServerOptionsProvider

import com.github.kornilova203.flameviewer.server.TreeManager

class CallTracesHandler(treeManager: TreeManager, logger: FlameLogger, optionsProvider: ServerOptionsProvider, findFile: FindFile)
    : AccumulativeTreeHandler(treeManager, logger, CALL_TRACES, optionsProvider, findFile)

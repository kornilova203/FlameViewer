package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.TreeType.CALL_TRACES

import com.github.korniloval.flameviewer.server.TreeManager

class CallTracesHandler(treeManager: TreeManager, logger: FlameLogger, findFile: FindFile)
    : AccumulativeTreeHandler(treeManager, logger, CALL_TRACES, findFile)

package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.converters.trees.Filter
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.server.FindFile
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.TreeManager
import com.github.korniloval.flameviewer.server.handlers.MethodsCounter.countMethods
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

class CallTreeCountMethods(private val treeManager: TreeManager, logger: FlameLogger, findFile: FindFile) : CountMethodsHandlerBase(logger, findFile) {
    override fun countMethods(file: File, filter: Filter?, decoder: QueryStringDecoder): Int {
        val tree = treeManager.getCallTree(file, null, null)
        if (tree == null) throw RequestHandlingException("Cannot get call tree for file $file")

        return countMethods(tree, filter)
    }
}

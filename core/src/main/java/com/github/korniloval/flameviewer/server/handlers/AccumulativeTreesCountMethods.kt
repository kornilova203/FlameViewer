package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.Filter
import com.github.korniloval.flameviewer.converters.trees.TreeType

import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.TreeManager
import com.github.korniloval.flameviewer.server.handlers.MethodsCounter.countMethods
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.File

class AccumulativeTreesCountMethods(private val treeManager: TreeManager, private val type: TreeType, logger: FlameLogger,
                                    findFile: FindFile) : CountMethodsHandlerBase(logger, findFile) {

    override fun countMethods(file: File, filter: Filter?, decoder: QueryStringDecoder): Int {
        val tree = getTree(file, decoder) ?: throw RequestHandlingException("Cannot get call tree for file $file")
        return countMethods(tree, filter)
    }

    fun getTree(file: File, decoder: QueryStringDecoder): Tree? {
        val methodName = getParameter(decoder, "method")
        val className = getParameter(decoder, "class")
        val desc = getParameter(decoder, "desc")
        return if (methodName != null && className != null && desc != null) {
            treeManager.getTree(file, type, className, methodName, desc, null)
        } else {
            treeManager.getTree(file, type, null)
        }
    }
}

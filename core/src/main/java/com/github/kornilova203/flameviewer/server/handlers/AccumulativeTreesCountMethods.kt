package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.converters.trees.Filter
import com.github.kornilova203.flameviewer.converters.trees.TreeType
import com.github.kornilova203.flameviewer.server.RequestHandlingException
import com.github.kornilova203.flameviewer.server.ServerUtil.getParameter
import com.github.kornilova203.flameviewer.server.TreeManager
import com.github.kornilova203.flameviewer.server.handlers.MethodsCounter.countMethods
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
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
            treeManager.getTree(file, type, className, methodName, desc)
        } else {
            treeManager.getTree(file, type)
        }
    }
}

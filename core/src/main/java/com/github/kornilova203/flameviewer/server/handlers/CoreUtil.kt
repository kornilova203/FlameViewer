package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.FlameLogger
import com.github.kornilova203.flameviewer.converters.trees.Filter
import com.github.kornilova203.flameviewer.server.RequestHandlingException
import com.github.kornilova203.flameviewer.server.ServerUtil.getParameter
import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import io.netty.handler.codec.http.QueryStringDecoder
import org.apache.commons.io.IOUtils
import java.io.File

@Throws(RequestHandlingException::class)
fun getFileName(decoder: QueryStringDecoder): String {
    return getParameter(decoder, "file")
            ?: throw RequestHandlingException("File is not specified. Uri: ${decoder.uri()}")
}

@Throws(RequestHandlingException::class)
fun getFilter(urlDecoder: QueryStringDecoder, logger: FlameLogger, ignoreParseErrors: Boolean = false): Filter? {
    val includePattern = nullize(getParameter(urlDecoder, "include")) ?: return null
    return Filter.tryCreate(includePattern, logger, ignoreParseErrors)
}

fun nullize(str: String?): String? {
    return if (str == null || str.isBlank()) null else str
}

fun treeBuilder(baseNode: Tree.Node.Builder): Tree.Builder = Tree.newBuilder().setBaseNode(baseNode)

fun treeBuilder(): Tree.Builder = Tree.newBuilder()

fun dfs(node: Tree.Node, skipFirst: Boolean = true, action: (Tree.Node) -> Unit) {
    if (!skipFirst) action(node)
    for (child in node.nodesList) {
        dfs(child, false, action)
    }
}

fun dfs(node: Tree.Node.Builder, skipFirst: Boolean = true, action: (Tree.Node.Builder) -> Unit) {
    if (!skipFirst) action(node)
    for (child in node.nodesBuilderList) {
        dfs(child, false, action)
    }
}

fun countNodes(node: Tree.Node.Builder): Int {
    var count = 0
    dfs(node) { count++ }
    return count
}

fun countNodes(node: Tree.Node): Int {
    var count = 0
    dfs(node) { count++ }
    return count
}

typealias FindFile = (name: String) -> File?
typealias FindResource = (uri: String) -> ByteArray?

object CoreUtil {
    val findResource: FindResource = { uri ->
        try {
            CoreUtil::class.java.getResourceAsStream("/static/$uri").use { stream ->
                IOUtils.toByteArray(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


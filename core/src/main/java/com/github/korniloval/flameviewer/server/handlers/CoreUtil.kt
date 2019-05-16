package com.github.korniloval.flameviewer.server.handlers

import com.github.kornilova_l.flamegraph.proto.TreeProtos.Tree
import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.Filter
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import io.netty.handler.codec.http.QueryStringDecoder
import org.apache.commons.io.IOUtils
import java.io.File

@Throws(RequestHandlingException::class)
fun getFileName(decoder: QueryStringDecoder): String {
    return getParameter(decoder, "file")
            ?: throw RequestHandlingException("File is not specified. Uri: ${decoder.uri()}")
}

@Throws(RequestHandlingException::class)
fun getFilter(urlDecoder: QueryStringDecoder, logger: FlameLogger): Filter? {
    val includePattern = getParameter(urlDecoder, "include") ?: return null
    return Filter.tryCreate(includePattern, logger)
}

fun treeBuilder(baseNode: Tree.Node.Builder): Tree.Builder = Tree.newBuilder().setBaseNode(baseNode)

fun treeBuilder(): Tree.Builder = Tree.newBuilder()

fun dfs(node: Tree.Node, action: (Tree.Node) -> Unit) {
    action(node)
    for (child in node.nodesList) {
        dfs(child, action)
    }
}

fun dfs(node: Tree.Node.Builder, action: (Tree.Node.Builder) -> Unit) {
    action(node)
    for (child in node.nodesBuilderList) {
        dfs(child, action)
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


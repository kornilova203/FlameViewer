package com.github.kornilova203.flameviewer.server

import com.github.kornilova203.flameviewer.converters.trees.Filter
import com.github.kornilova203.flameviewer.converters.trees.ToTreesSetConverterFactory
import com.github.kornilova203.flameviewer.converters.trees.TreeType
import com.github.kornilova203.flameviewer.converters.trees.TreesSet
import com.github.kornilova203.flameviewer.converters.trees.hotspots.HotSpot
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import java.io.File
import java.util.concurrent.atomic.AtomicReference

open class TreeManager(private val toTreesSet: ToTreesSetConverterFactory) {
    protected val currentFile = AtomicReference<File?>()
    protected val currentTreesSet = AtomicReference<TreesSet?>()

    @Synchronized
    fun getCallTree(file: File,
                    filter: Filter?,
                    threadsIds: List<Int>?): TreesProtos.Trees? {
        updateTreesSet(file)
        return currentTreesSet.get()?.getCallTree(filter, threadsIds)
    }

    protected open fun updateTreesSet(file: File) {
        val curFile = currentFile.get()
        if (curFile == null || file.absolutePath != curFile.absolutePath) {
            currentFile.set(file)
            currentTreesSet.set(toTreesSet.create(file)?.convert(null))
        }
    }

    @Synchronized
    fun getTree(logFile: File, treeType: TreeType): TreeProtos.Tree? {
        updateTreesSet(logFile)
        return currentTreesSet.get()?.getTree(treeType)
    }

    @Synchronized
    fun getTree(logFile: File, treeType: TreeType, className: String, methodName: String, desc: String): TreeProtos.Tree? {
        updateTreesSet(logFile)
        return currentTreesSet.get()?.getTree(treeType, className, methodName, desc)

    }

    @Synchronized
    fun getHotSpots(file: File): List<HotSpot> {
        updateTreesSet(file)
        return currentTreesSet.get()?.getHotSpots() ?: emptyList()
    }

    @Synchronized
    fun getCallTreesPreview(file: File, filter: Filter?): TreesPreview? {
        updateTreesSet(file)
        return currentTreesSet.get()?.getTreesPreview(filter)
    }
}


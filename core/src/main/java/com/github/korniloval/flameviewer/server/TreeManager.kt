package com.github.korniloval.flameviewer.server

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.github.korniloval.flameviewer.converters.trees.Filter
import com.github.korniloval.flameviewer.converters.trees.ToTreesSetConverterFactory
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.converters.trees.TreesSet
import com.github.korniloval.flameviewer.converters.trees.hotspots.HotSpot
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class TreeManager(private val toTreesSetConverterFactory: ToTreesSetConverterFactory) {
    private val currentFile = AtomicReference<File?>()
    private val currentTreesSet = AtomicReference<TreesSet?>()
    private val lastUpdate = AtomicLong(0)
    private val timeDelta = 1000 * 60 * 2

    init {
        val watchLastUpdate = Thread {
            while (true) {
                try {
                    Thread.sleep(10000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                checkLastUpdate()
            }
        }
        watchLastUpdate.isDaemon = true
        watchLastUpdate.start()
    }

    @Synchronized
    private fun checkLastUpdate() {
        if (System.currentTimeMillis() - lastUpdate.get() >= timeDelta) {
            currentTreesSet.set(null)
            currentFile.set(null)
            lastUpdate.set(System.currentTimeMillis())
        }
    }

    @Synchronized
    fun getCallTree(file: File,
                    filter: Filter?,
                    threadsIds: List<Int>?): TreesProtos.Trees? {
        updateTreesSet(file)
        return currentTreesSet.get()?.getCallTree(filter, threadsIds)
    }

    private fun updateTreesSet(file: File) {
        val curFile = currentFile.get()
        if (curFile == null || file.absolutePath != curFile.absolutePath) {
            currentFile.set(file)
            currentTreesSet.set(toTreesSetConverterFactory.create(file)?.convert())
        }
        updateLastTime()
    }

    @Synchronized
    fun getTree(logFile: File, treeType: TreeType, filter: Filter?): TreeProtos.Tree? {
        updateTreesSet(logFile)
        return currentTreesSet.get()?.getTree(treeType, filter)
    }

    @Synchronized
    fun getTree(logFile: File, treeType: TreeType, className: String, methodName: String, desc: String, filter: Filter?): TreeProtos.Tree? {
        updateTreesSet(logFile)
        return currentTreesSet.get()?.getTree(treeType, className, methodName, desc, filter)

    }

    @Synchronized
    fun getHotSpots(file: File): List<HotSpot> {
        updateTreesSet(file)
        return currentTreesSet.get()?.getHotSpots() ?: emptyList()
    }

    @Synchronized
    fun updateLastTime() {
        lastUpdate.set(System.currentTimeMillis())
    }

    @Synchronized
    fun getCallTreesPreview(file: File, filter: Filter?): TreesPreview? {
        updateTreesSet(file)
        return currentTreesSet.get()?.getTreesPreview(filter)
    }
}


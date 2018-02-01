package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager
import com.github.kornilova_l.flamegraph.plugin.converters.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.HotSpot
import com.github.kornilova_l.flamegraph.plugin.server.trees.converters.flamegraph_format_trees.TreesSetImpl
import com.github.kornilova_l.flamegraph.plugin.server.trees.ser_trees.SerTreesSet
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.openapi.diagnostic.Logger
import java.io.File

object TreeManager {
    private var currentFile: File? = null
    @Volatile
    private var currentTreesSet: TreesSet? = null
    private var lastUpdate: Long = 0

    init {
        val thisTreeManager = this
        val watchLastUpdate = Thread {
            while (true) {
                try {
                    Thread.sleep(10000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                if (thisTreeManager.longTimePassedSinceUpdate()) {
                    thisTreeManager.removeTreesSet()
                }
            }
        }
        watchLastUpdate.isDaemon = true
        watchLastUpdate.start()
    }

    private fun removeTreesSet() {
        currentTreesSet = null
        currentFile = null
        lastUpdate = System.currentTimeMillis()
    }

    @Synchronized
    private fun longTimePassedSinceUpdate(): Boolean {
        return System.currentTimeMillis() - lastUpdate >= 30000
    }

    @Synchronized
    fun getCallTree(logFile: File?,
                    filter: Filter?,
                    threadsIds: List<Int>?): TreesProtos.Trees? {
        logFile ?: return null
        updateTreesSet(logFile)
        val currentTreesSet = this.currentTreesSet ?: return null
        return currentTreesSet.getCallTree(filter, threadsIds)
    }

    private fun updateTreesSet(logFile: File) {
        if (currentFile == null || logFile.absolutePath != currentFile!!.absolutePath) {
            currentFile = logFile
            if (ProfilerToFlamegraphConverter.getFileExtension(logFile.name) == "ser") {
                currentTreesSet = SerTreesSet(logFile)
                return
            }
            val parentDirName = PluginFileManager.getParentDirName(logFile)
            if (parentDirName == null) {
                LOG.error("Cannot find parent directory of log file")
                return
            }
            val callTraces = FileToCallTracesConverter.convert(parentDirName, logFile)
            if (callTraces == null) {
                LOG.error("Cannot convert file " + logFile)
                return
            }
            currentTreesSet = TreesSetImpl(callTraces)
        }
    }

    @Synchronized
    fun getTree(logFile: File?, treeType: TreeType, filter: Filter?): TreeProtos.Tree? {
        logFile ?: return null
        updateTreesSet(logFile)
        val currentTreesSet = this.currentTreesSet ?: return null
        return currentTreesSet.getTree(treeType, filter)
    }

    @Synchronized
    fun getTree(logFile: File?,
                treeType: TreeType,
                className: String,
                methodName: String,
                desc: String,
                filter: Filter?): TreeProtos.Tree? {
        logFile ?: return null
        updateTreesSet(logFile)
        val currentTreesSet = this.currentTreesSet ?: return null
        return currentTreesSet.getTree(treeType, className, methodName, desc, filter)

    }

    @Synchronized
    fun getHotSpots(logFile: File?): List<HotSpot>? {
        logFile ?: return null
        updateTreesSet(logFile)
        val currentTreesSet = this.currentTreesSet ?: return null
        return currentTreesSet.hotSpots
    }

    @Synchronized
    fun updateLastTime() {
        lastUpdate = System.currentTimeMillis()
    }

    @Synchronized
    fun getCallTreesPreview(logFile: File?, filter: Filter?): TreesPreview? {
        logFile ?: return null
        updateTreesSet(logFile)
        val currentTreesSet = this.currentTreesSet ?: return null
        return currentTreesSet.getTreesPreview(filter)
    }

    enum class TreeType {
        OUTGOING_CALLS,
        INCOMING_CALLS
    }

    private val LOG = Logger.getInstance(PluginFileManager::class.java)
}


package com.github.korniloval.flameviewer.trees

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.FileToCallTreeConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverterFactory
import com.github.korniloval.flameviewer.converters.calltree.fierix.FierixToCallTreeConverterFactory.Companion.isFierixExtension
import com.github.korniloval.flameviewer.trees.hot.spots.HotSpot
import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.github.kornilova_l.flamegraph.proto.TreesPreviewProtos.TreesPreview
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.PathUtil
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
            val extension = PathUtil.getFileExtension(logFile.name)
            if (isFierixExtension(extension)) {
                currentTreesSet = TreesSetImpl(FileToCallTreeConverterFactory.convert(FierixToCallTreeConverterFactory.EXTENSION, logFile)!!)
                return
            }
            var parentDirName = PluginFileManager.getParentDirName(logFile)
            if (parentDirName == null) {
                LOG.error("Cannot find parent directory of the log file: ${logFile.absolutePath}")
                return
            }
            if (parentDirName == "ser") {
                /* ser extension is deprecated */
                parentDirName = FierixToCallTreeConverterFactory.EXTENSION
            }
            /* try to convert to call tree */
            val callTree = FileToCallTreeConverterFactory.convert(parentDirName, logFile)
            currentTreesSet =
                    if (callTree != null) {
                        TreesSetImpl(callTree)
                    } else {
                        /* try to convert to call traces */
                        val callTraces = FileToCallTracesConverterFactory.convert(parentDirName, logFile)
                        if (callTraces == null) {
                            LOG.error("Cannot convert file: ${logFile.absolutePath}")
                            return
                        }
                        TreesSetImpl(callTraces)
                    }
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
        return currentTreesSet.getHotSpots()
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
        CALL_TRACES,
        BACK_TRACES
    }

    private val LOG = Logger.getInstance(PluginFileManager::class.java)
}


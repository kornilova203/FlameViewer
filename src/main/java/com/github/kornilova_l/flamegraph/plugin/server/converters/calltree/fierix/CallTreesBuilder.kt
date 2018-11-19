package com.github.kornilova_l.flamegraph.plugin.server.converters.calltree.fierix

import com.github.kornilova_l.flamegraph.plugin.pleaseReportIssue
import com.github.kornilova_l.flamegraph.proto.EventProtos
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class CallTreesBuilder(logFile: File) {
    private val treesMap = HashMap<Long, CTBuilder>()
    private val classNames = HashMap<Long, String>()
    private val threadsNames = HashMap<Long, String>()
    var trees: TreesProtos.Trees? = null
        private set

    init {
        try {
            FileInputStream(logFile).use { inputStream ->
                processEvents(inputStream)
                val startTimeOfFirstThread = getStartTimeOfFirstThread(treesMap)
                trees = hashMapToTrees(treesMap, startTimeOfFirstThread)
            }
        } catch (e: IOException) {
            LOG.error(e)
        }

    }

    @Throws(IOException::class)
    private fun processEvents(inputStream: InputStream) {
        var event: EventProtos.Event? = EventProtos.Event.parseDelimitedFrom(inputStream)
        while (event != null) {
            when (event.typeCase) {
                EventProtos.Event.TypeCase.METHODEVENT -> addMethodEvent(event)
                EventProtos.Event.TypeCase.NEWCLASS -> registerClass(event)
                EventProtos.Event.TypeCase.NEWTHREAD -> threadsNames[event.newThread.id] = event.newThread.name
                EventProtos.Event.TypeCase.TYPE_NOT_SET -> throw RuntimeException("$pleaseReportIssue: Event without type")
                else -> throw RuntimeException("$pleaseReportIssue: Event without type")
            }
            event = EventProtos.Event.parseDelimitedFrom(inputStream)
        }
    }

    private fun registerClass(event: EventProtos.Event) {
        val className = event.newClass.name
        classNames[event.newClass.id] = className.replace('/', '.')
    }

    private fun addMethodEvent(event: EventProtos.Event) {
        val methodEvent = event.methodEvent
        val ctBuilder = getCTBuilder(methodEvent)
        val className = classNames[methodEvent.classNameId]
                ?: throw RuntimeException("$pleaseReportIssue (also please upload this ser file): Class name is not known. id = ${methodEvent.classNameId}")
        ctBuilder.addEvent(methodEvent, className)
    }

    private fun getCTBuilder(methodEvent: EventProtos.Event.MethodEvent): CTBuilder {
        val threadName = threadsNames[methodEvent.threadId]
        if (threadName == null) {
            LOG.debug("Thread name is not known. MethodEvent: " + methodEvent.toString())
            return treesMap.computeIfAbsent(
                    methodEvent.threadId
            ) { CTBuilder(methodEvent.startTime, "") }
        }
        return treesMap.computeIfAbsent(
                methodEvent.threadId
        ) { CTBuilder(methodEvent.startTime, threadName) }
    }

    private fun getStartTimeOfFirstThread(treesMap: Map<Long, CTBuilder>): Long {
        val treesList = ArrayList(treesMap.values)
        val size = treesList.size
        if (size == 0) {
            return 0
        }
        var startTimeOfFirstThread = treesList[0].threadStartTime
        for (i in 1 until size) {
            if (treesList[i].threadStartTime < startTimeOfFirstThread) {
                startTimeOfFirstThread = treesList[i].threadStartTime
            }
        }
        return startTimeOfFirstThread
    }

    companion object {
        private val LOG = com.intellij.openapi.diagnostic.Logger.getInstance(CallTreesBuilder::class.java)

        private fun hashMapToTrees(trees: Map<Long, CTBuilder>,
                                   startTimeOfFirstThread: Long): TreesProtos.Trees? {
            val treesBuilder = TreesProtos.Trees.newBuilder()
            for (oTBuilder in trees.values) {
                val tree = oTBuilder.getBuiltTree(startTimeOfFirstThread)
                if (tree != null) {
                    treesBuilder.addTrees(
                            tree
                    )
                }
            }
            return if (treesBuilder.treesCount == 0) {
                null
            } else treesBuilder.build()
        }
    }
}

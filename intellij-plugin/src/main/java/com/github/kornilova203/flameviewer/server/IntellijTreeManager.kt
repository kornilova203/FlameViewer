package com.github.kornilova203.flameviewer.server

import com.github.kornilova203.flameviewer.converters.trees.ToTreesSetConverterFactory
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class IntellijTreeManager(toTreesSet: ToTreesSetConverterFactory) : TreeManager(toTreesSet) {
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
    fun updateLastTime() {
        lastUpdate.set(System.currentTimeMillis())
    }

    override fun updateTreesSet(file: File) {
        super.updateTreesSet(file)
        updateLastTime()
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.trees

import com.github.kornilova_l.flamegraph.proto.TreeProtos
import com.intellij.openapi.extensions.ExtensionPointName
import java.io.File

/**
 * Builds call traces
 */
abstract class ProfilerToCallTracesConverter {
    companion object {
        private val EP_NAME = ExtensionPointName.create<ProfilerToCallTracesConverter>("com.github.kornilovaL.flamegraphProfiler.profilerToCallTracesConverter")

        fun convert(file: File): TreeProtos.Tree? {
            for (extension in EP_NAME.extensions) {
                if (extension.isSupported(file)) {
                    return extension.convert(file)
                }
            }
            return null
        }
    }

    /**
     * Is file supported by this converter
     */
    abstract fun isSupported(file: File): Boolean

    /**
     * Convert file to call traces tree
     */
    abstract fun convert(file: File): TreeProtos.Tree
}
package com.github.kornilova_l.flamegraph.plugin.server.converters.calltree.fierix

import com.github.kornilova_l.flamegraph.plugin.server.converters.calltree.FileToCallTreeConverter
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import com.intellij.util.PathUtil
import java.io.File

class FierixToCallTreeConverter : FileToCallTreeConverter() {

    override fun getId(): String = EXTENSION

    override fun isSupported(file: File): Boolean {
        val fileExtension = PathUtil.getFileExtension(file.name)
        /* .ser is an old deprecated extension */
        return fileExtension == EXTENSION || fileExtension == "ser"
    }

    override fun convert(file: File): TreesProtos.Trees = CallTreesBuilder(file).trees!!

    companion object {
        const val EXTENSION = "fierix"
    }
}

package com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_tree.fierix

import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_call_tree.FileToCallTreeConverter
import com.github.kornilova_l.flamegraph.plugin.server.converters.file_to_file.ProfilerToFlamegraphConverter
import com.github.kornilova_l.flamegraph.proto.TreesProtos
import java.io.File

class FierixToCallTreeConverter : FileToCallTreeConverter() {

    override fun getId(): String = EXTENSION

    override fun isSupported(file: File): Boolean {
        val fileExtension = ProfilerToFlamegraphConverter.getFileExtension(file.name)
        /* .ser is an old deprecated extension */
        return fileExtension == EXTENSION || fileExtension == "ser"
    }

    override fun convert(file: File): TreesProtos.Trees = CallTreesBuilder(file).trees!!

    companion object {
        const val EXTENSION = "fierix"
    }
}

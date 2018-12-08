package com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.yourkit.csv

import com.github.kornilova_l.flamegraph.plugin.server.converters.calltraces.FileToCallTracesConverterFactory
import com.github.kornilova_l.flamegraph.plugin.server.converters.cflamegraph.csv.yourkit.YourkitCsvToCFlamegraphConverterFactory.Companion.isYourkitCsv
import java.io.File


@Deprecated("When a new csv file is added it's converted with YourkitCsvToCFlamegraphConverterFactory. " +
        "This converter is to support already uploaded csv files.")
class YourkitCsvToCallTracesConverterFactory : FileToCallTracesConverterFactory {
    override fun getId(): String = "yourkit"

    override fun isSupported(file: File): Boolean = isYourkitCsv(file)

    override fun create(file: File) = YourkitToCallTracesConverter(file)
}

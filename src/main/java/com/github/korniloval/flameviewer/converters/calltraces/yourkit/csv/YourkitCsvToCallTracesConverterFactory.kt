package com.github.korniloval.flameviewer.converters.calltraces.yourkit.csv

import com.github.korniloval.flameviewer.converters.calltraces.FileToCallTracesConverterFactory
import com.github.korniloval.flameviewer.converters.cflamegraph.yourkit.csv.YourkitCsvToCFlamegraphConverterFactory.Companion.isYourkitCsv
import java.io.File


@Deprecated("When a new csv file is added it's converted with YourkitCsvToCFlamegraphConverterFactory. " +
        "This converter is to support already uploaded csv files.")
class YourkitCsvToCallTracesConverterFactory : FileToCallTracesConverterFactory {
    override val id = "yourkit"

    override fun isSupported(file: File): Boolean = isYourkitCsv(file)

    override fun create(file: File) = YourkitToCallTracesConverter(file)
}

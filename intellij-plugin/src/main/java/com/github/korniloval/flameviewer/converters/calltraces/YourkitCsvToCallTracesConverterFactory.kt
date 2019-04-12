package com.github.korniloval.flameviewer.converters.calltraces

import com.github.korniloval.flameviewer.converters.cflamegraph.isYourkitCsv
import java.io.File


@Deprecated("When a new csv file is added it's converted with YourkitCsvToCFlamegraphConverterFactory. " +
        "This converter is to support already uploaded csv files.")
class YourkitCsvToCallTracesConverterFactory : ToCallTracesIdentifiedConverterFactory {
    override val id = "yourkit"

    @Override
    override fun isSupported(file: File): Boolean = isYourkitCsv(file)

    override fun create(file: File) = YourkitToCallTracesConverter(file)
}

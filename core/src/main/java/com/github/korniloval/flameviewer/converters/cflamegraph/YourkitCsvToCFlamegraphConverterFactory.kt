package com.github.korniloval.flameviewer.converters.cflamegraph

import java.io.File

object YourkitCsvToCFlamegraphConverterFactory : ToCFlamegraphConverterFactory {
    override fun isSupported(file: File) = isYourkitCsv(file)
    override fun create(file: File) = if (isSupported(file)) YourkitCsvToCFlamegraphConverter(file) else null
}

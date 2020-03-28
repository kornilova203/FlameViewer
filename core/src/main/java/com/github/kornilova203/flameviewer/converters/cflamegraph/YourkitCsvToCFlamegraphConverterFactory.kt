package com.github.kornilova203.flameviewer.converters.cflamegraph

import java.io.File

object YourkitCsvToCFlamegraphConverterFactory : ToCFlamegraphConverterFactory {
    override fun isSupported(file: File) = isYourkitCsv(file)
    override fun create(file: File) = if (isSupported(file)) YourkitCsvToCFlamegraphConverter(file) else null
}

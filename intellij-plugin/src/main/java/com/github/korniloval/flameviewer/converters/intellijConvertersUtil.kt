package com.github.korniloval.flameviewer.converters

import com.intellij.openapi.diagnostic.Logger
import java.io.File

fun <T> tryCreate(factories: Array<out IdentifiedConverterFactory<T>>, file: File, logger: Logger): Converter<out T>? {
    val converterId = file.parentFile?.name
    if (converterId == null) {
        logger.warn("Cannot find parent file. File: $file")
        return null
    }
    for (factory in factories) {
        if (factory.id == converterId) {
            val converter = factory.create(file)
            if (converter != null) return converter
        }
    }
    return null
}

fun getConverterId(factories: Array<out IdentifiedConverterFactory<*>>, file: File): String? {
    for (factory in factories) {
        if (factory.isSupported(file)) {
            return factory.id
        }
    }
    return null
}

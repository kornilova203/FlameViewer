package com.github.korniloval.flameviewer.converters

import java.io.File

fun <T> tryConvert(factories: Array<out ConverterFactory<T>>, file: File, errorHandler: (e: ConversionException) -> Unit): T? {
    for (factory in factories) {
        val converter = factory.create(file)
        if (converter != null) return try {
            converter.convert()
        } catch (e: ConversionException) {
            errorHandler(e)
            null
        }
    }
    return null
}

fun <T> tryConvert(factories: Array<out IdentifiedConverterFactory<T>>, converterId: String, file: File, errorHandler: (e: ConversionException) -> Unit): T? {
    for (factory in factories) {
        if (factory.id == converterId) {
            val converter = factory.create(file) ?: return null
            return try {
                converter.convert()
            } catch (e: ConversionException) {
                errorHandler(e)
                null
            }
        }
    }
    return null
}

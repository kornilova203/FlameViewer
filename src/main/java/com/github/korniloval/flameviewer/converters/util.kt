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

object FramesParsingUtil {
    fun getLastSpacePosBeforeParams(name: String, openBracketPos: Int): Int {
        for (i in openBracketPos - 1 downTo 0) {
            if (name[i] == ' ') {
                return i
            }
        }
        return -1
    }

    /**
     * We do not know if name contains return value.
     * It may even not contain class name
     */
    fun getClassName(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
        var lastDot = -1
        for (i in parametersPos - 1 downTo 0) {
            if (name[i] == '.') {
                lastDot = i
                break
            }
        }
        if (lastDot == -1) {
            return null
        }
        return name.substring(lastSpacePosBeforeParams + 1, lastDot)
    }

    fun getDescription(name: String, parametersPos: Int, lastSpacePosBeforeParams: Int): String? {
        return if (parametersPos == name.length && lastSpacePosBeforeParams == -1) {
            null
        } else if (lastSpacePosBeforeParams == -1) { // if only parameters
            name.substring(parametersPos, name.length)
        } else if (parametersPos == name.length) { // if only ret val
            "()" + name.substring(0, lastSpacePosBeforeParams)
        } else { // if both
            name.substring(parametersPos, name.length) + name.substring(0, lastSpacePosBeforeParams)
        }
    }

    fun getMethodName(name: String, parametersPos: Int): String {
        for (i in parametersPos - 1 downTo 0) {
            val c = name[i]
            if (c == '.' || c == ' ') {
                return name.substring(i + 1, parametersPos)
            }
        }
        return name.substring(0, parametersPos)
    }
}


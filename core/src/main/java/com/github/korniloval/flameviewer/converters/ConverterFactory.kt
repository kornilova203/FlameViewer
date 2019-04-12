package com.github.korniloval.flameviewer.converters

import java.io.File

interface ConverterFactory<out T> {

    /**
     * @return null if the file is not supported by converter
     * @param file will be deleted after calling this method
     */
    fun create(file: File): Converter<out T>?

    fun isSupported(file: File): Boolean = true
}
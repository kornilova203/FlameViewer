package com.github.korniloval.flameviewer.converters

import java.io.File

interface IdentifiedConverterFactory<T> : ConverterFactory<T> {
    /**
     * Is file supported by this builder.
     * This method is called ones when file is uploaded.
     */
    fun isSupported(file: File): Boolean

    val id: String
}
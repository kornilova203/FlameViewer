package com.github.korniloval.flameviewer.converters

import java.io.File

/**
 * [id] is used to locate file in directory where plugin stores converted files.
 * So there is no need to detect file format each time before visualization
 */
interface IdentifiedConverterFactory<T> : ConverterFactory<T> {
    val id: String

    /**
     * This method is called ones when file is uploaded.
     */
    override fun isSupported(file: File): Boolean
}
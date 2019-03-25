package com.github.korniloval.flameviewer.converters

interface IdentifiedConverterFactory<out T> : ConverterFactory<T> {
    val id: String
}

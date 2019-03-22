package com.github.korniloval.flameviewer.converters

interface Converter<out T> {
    @Throws(ConversionException::class)
    fun convert(): T
}
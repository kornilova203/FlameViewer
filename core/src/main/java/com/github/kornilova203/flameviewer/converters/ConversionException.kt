package com.github.kornilova203.flameviewer.converters

/**
 * @author Liudmila Kornilova
 **/
class ConversionException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
}

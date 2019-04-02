package com.github.korniloval.flameviewer


interface FlameLogger {
    fun error(msg: String, t: Throwable? = null)
    fun warn(msg: String, t: Throwable? = null)
    fun info(msg: String)
}

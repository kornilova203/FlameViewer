package com.github.kornilova203.flameviewer

import com.intellij.openapi.diagnostic.Logger

class LoggerAdapter(private val logger: Logger) : FlameLogger {
    override fun error(msg: String, t: Throwable?) = logger.error(msg, t)
    override fun warn(msg: String, t: Throwable?) = logger.warn(msg, t)
    override fun info(msg: String) = logger.info(msg)
}

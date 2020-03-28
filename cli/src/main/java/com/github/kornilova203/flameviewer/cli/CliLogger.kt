package com.github.kornilova203.flameviewer.cli

import com.github.kornilova203.flameviewer.FlameLogger

class CliLogger : FlameLogger {
    override fun info(msg: String) = println("INFO: $msg")

    override fun warn(msg: String, t: Throwable?) {
        System.err.println("ERROR: $msg")
        t?.printStackTrace()
    }

    override fun error(msg: String, t: Throwable?) {
        System.err.println("ERROR: $msg")
        t?.printStackTrace()
    }
}

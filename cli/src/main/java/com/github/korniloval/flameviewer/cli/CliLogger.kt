package com.github.korniloval.flameviewer.cli

import com.github.korniloval.flameviewer.FlameLogger

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

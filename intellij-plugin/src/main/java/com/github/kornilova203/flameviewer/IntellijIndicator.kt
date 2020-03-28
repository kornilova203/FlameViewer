package com.github.kornilova203.flameviewer

import com.intellij.openapi.progress.ProgressIndicator

class IntellijIndicator(private val indicator: ProgressIndicator) : FlameIndicator {
    override fun checkCanceled() = indicator.checkCanceled()
}

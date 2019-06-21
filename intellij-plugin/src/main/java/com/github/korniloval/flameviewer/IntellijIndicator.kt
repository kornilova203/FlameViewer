package com.github.korniloval.flameviewer

import com.intellij.openapi.progress.ProgressIndicator

class IntellijIndicator(private val indicator: ProgressIndicator) : FlameIndicator {
    override fun checkCanceled() = indicator.checkCanceled()
}

package com.github.kornilova203.flameviewer

import com.github.kornilova203.flameviewer.server.CALL_TRACES_NAME
import com.github.kornilova203.flameviewer.server.FileUploader
import com.github.kornilova203.flameviewer.server.getUrlBuilderBase
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile


class OpenFileInFlameViewerAction : DumbAwareAction() {
    private val LOG = Logger.getInstance(OpenFileInFlameViewerAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val file = getFile(e) ?: return
        val project = e.project ?: return

        ProgressManager
                .getInstance()
                .run(object : Task.Backgroundable(project, "Processing file ${file.name} for FlameViewer", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val bytes = file.inputStream.readBytes()
                        indicator.isIndeterminate = true
                        val res = FileUploader().upload(file.name, bytes, 1, 1, IntellijIndicator(indicator))

                        indicator.checkCanceled()
                        if (res) {
                            val urlBuilder = getUrlBuilderBase()
                                    .addPathSegments(CALL_TRACES_NAME)
                                    .addQueryParameter("file", file.name)

                            BrowserUtil.browse(urlBuilder.build().toUrl())
                        } else {
                            LOG.info("File format is unsupported")
                        }
                    }
                })
    }

    private fun getFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        return if (file == null || file.isDirectory) null else file
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = getFile(e) != null
    }
}

package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.server.CALL_TRACES_NAME
import com.github.korniloval.flameviewer.server.FileUploader
import com.github.korniloval.flameviewer.server.getUrlBuilderBase
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.VirtualFile


class OpenFileInFlameViewerAction : DumbAwareAction() {
    private val LOG = Logger.getInstance(OpenFileInFlameViewerAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val file = getFile(e) ?: return
        val project = e.project ?: return

        val task: () -> Unit = {
            val bytes = file.inputStream.readBytes()
            val res = FileUploader().upload(file.name, bytes, 1, 1)

            if (res) {
                val urlBuilder = getUrlBuilderBase()
                        .addPathSegments(CALL_TRACES_NAME)
                        .addQueryParameter("file", file.name)

                BrowserUtil.browse(urlBuilder.build().url())
            } else {
                LOG.info("File format is unsupported")
            }
        }
        ProgressManager
                .getInstance()
                .runProcessWithProgressSynchronously(
                        task,
                        "Processing file ${file.name} for FlameViewer",
                        false, project)
    }

    private fun getFile(e: AnActionEvent): VirtualFile? {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        return if (file == null || file.isDirectory) null else file
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = getFile(e) != null
    }
}

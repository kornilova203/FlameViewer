package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.ServerUtil.sendBytes
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

object HtmlHandler : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        sendBytes(
                ctx,
                "text/html",
                renderPage(
                        "${decoder.path()}.html",
                        getParameter(decoder, "file"),
                        "",
                        getParameter(decoder, "include"),
                        getParameter(decoder, "exclude")
                )
        )
        return true
    }

    private fun renderPage(htmlFilePath: String,
                           fileName: String?,
                           projectName: String,
                           include: String?,
                           exclude: String?): ByteArray {
        val bytes = PluginFileManager.getStaticFile(htmlFilePath)
                ?: throw RequestHandlingException("Cannot render page $htmlFilePath project: $projectName file $fileName include $include exclude $exclude")
        val fileContent = String(bytes)
        val filterParameters = getFilterAsGetParameters(include, exclude)
        val replacement = if (fileName == null) "" else "file=$fileName&"

        return fileContent
                .lines().map { line ->
                    line.replace("{{ projectName }}", projectName)
                            .replace("{{ fileParam }}", replacement)
                            .replace("{{ filter }}", filterParameters)
                }
                .toList().joinToString("").toByteArray()
    }

    private fun getFilterAsGetParameters(include: String?, exclude: String?): String {
        var res = ""
        if (include != null) {
            res += "&include=$include"
        }
        if (exclude != null) {
            res += "&exclude=$exclude"
        }
        return res
    }
}

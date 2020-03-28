package com.github.kornilova203.flameviewer.server.handlers

import com.github.kornilova203.flameviewer.server.RequestHandlerBase
import com.github.kornilova203.flameviewer.server.RequestHandlingException
import com.github.kornilova203.flameviewer.server.ServerUtil.getParameter
import com.github.kornilova203.flameviewer.server.ServerUtil.sendBytes
import com.github.kornilova203.flameviewer.server.handlers.CoreUtil.findResource
import com.github.kornilova203.flameviewer.server.handlers.StaticHandler.Companion.getFileUri
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

object HtmlHandler : RequestHandlerBase() {
    override fun processGet(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val decoder = QueryStringDecoder(request.uri())
        val uri = getFileUri(decoder)
        sendBytes(
                ctx,
                "text/html",
                renderPage(
                        "$uri.html",
                        getParameter(decoder, "file"),
                        getParameter(decoder, "include"),
                        getParameter(decoder, "exclude")
                )
        )
        return true
    }

    private fun renderPage(uri: String,
                           fileName: String?,
                           include: String?,
                           exclude: String?): ByteArray {
        val bytes = findResource(uri)
                ?: throw RequestHandlingException("Cannot render page $uri file $fileName include $include exclude $exclude")
        val fileContent = String(bytes)
        val filterParameters = getFilterAsGetParameters(include, exclude)
        val replacement = if (fileName == null) "" else "file=$fileName&"

        return fileContent
                .lines().map { line ->
                    line.replace("{{ fileParam }}", replacement)
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

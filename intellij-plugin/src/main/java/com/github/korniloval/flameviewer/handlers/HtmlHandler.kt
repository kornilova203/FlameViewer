package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import com.github.korniloval.flameviewer.server.ServerUtil.sendBytes
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import kotlin.streams.toList

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
        val staticFile = PluginFileManager.getStaticFile(htmlFilePath)
        val filterParameters = getFilterAsGetParameters(include, exclude)
        try {
            BufferedReader(FileReader(staticFile)).use { bufferedReader ->
                return bufferedReader.lines()
                        .map { line ->
                            val replacement = if (fileName == null)
                                ""
                            else
                                "file=$fileName&"
                            line.replace("{{ projectName }}", projectName)
                                    .replace("{{ fileParam }}", replacement)
                                    .replace("{{ filter }}", filterParameters)
                        }
                        .toList().joinToString("").toByteArray()
            }
        } catch (e: IOException) {
            throw RuntimeException("Cannot render page " + htmlFilePath + " project: " +
                    projectName + " file " + fileName + " include " + include + " exclude " + exclude, e)
        }

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

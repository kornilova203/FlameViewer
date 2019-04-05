package com.github.korniloval.flameviewer.handlers

import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.RequestHandler
import com.github.korniloval.flameviewer.server.ServerUtil.sendBytes
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.QueryStringDecoder
import org.apache.commons.io.IOUtils
import java.io.FileInputStream

class StaticHandler(private val contentType: String) : RequestHandler {
    override fun process(request: FullHttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileUri = QueryStringDecoder(request.uri()).path()
        val staticFile = PluginFileManager.getStaticFile(fileUri)
                ?: throw RuntimeException("Cannot find static files. File uri: $fileUri")
        FileInputStream(staticFile).use { inputStream -> sendBytes(ctx, contentType, IOUtils.toByteArray(inputStream)) }
        return true
    }
}

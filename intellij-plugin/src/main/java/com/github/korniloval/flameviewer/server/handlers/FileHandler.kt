package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.PluginFileManager
import com.github.korniloval.flameviewer.server.FileUploader
import com.github.korniloval.flameviewer.server.RequestHandlerBase
import com.github.korniloval.flameviewer.server.ServerUtil.sendStatus
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufHolder
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class FileHandler(private val fileManager: PluginFileManager, private val fileUploader: FileUploader, private val logger: FlameLogger) : RequestHandlerBase() {
    override fun processPost(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        val fileParts = getFileParts(request)
        if (fileParts == null) {
            sendStatus(HttpResponseStatus.BAD_REQUEST, ctx.channel(), "Request does not contain File-Part header")
            return true
        }
        val currentPart = fileParts[0]
        val totalPartsCount = fileParts[1]
        logger.info("Got file: $fileName")
        val bytes = getBytes((request as ByteBufHolder).content())
        fileUploader.upload(fileName, bytes, currentPart, totalPartsCount, null)
        sendStatus(HttpResponseStatus.OK, ctx.channel(), "File part was successfully uploaded")
        return true
    }

    override fun processDelete(request: HttpRequest, ctx: ChannelHandlerContext): Boolean {
        val fileName = request.headers().get("File-Name")
        logger.info("Delete file: $fileName")
        fileManager.deleteFile(fileName)
        sendStatus(HttpResponseStatus.OK, ctx.channel())
        return true
    }

    private fun getBytes(byteBuf: ByteBuf): ByteArray {
        val bytes = ByteArray(byteBuf.readableBytes())
        byteBuf.readBytes(bytes)
        return bytes
    }

    /**
     * @return information about what part was sent
     */
    private fun getFileParts(request: HttpRequest): IntArray? {
        val filePartsString = request.headers().get("File-Part")
        val fileParts = filePartsString.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (fileParts.size != 2) {
            logger.error("Header File-Parts does not contain information in format '<current part>/<total parts>'. $filePartsString")
        }
        try {
            return intArrayOf(Integer.parseInt(fileParts[0]), Integer.parseInt(fileParts[1]))
        } catch (e: NumberFormatException) {
            logger.error("Header File-Parts does not contain information in format '<current part>/<total parts>'. $filePartsString", e)
        }

        return null
    }
}

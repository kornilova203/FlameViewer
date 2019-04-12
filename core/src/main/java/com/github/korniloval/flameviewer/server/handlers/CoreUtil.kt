package com.github.korniloval.flameviewer.server.handlers

import com.github.korniloval.flameviewer.FlameLogger
import com.github.korniloval.flameviewer.converters.trees.Filter
import com.github.korniloval.flameviewer.server.RequestHandlingException
import com.github.korniloval.flameviewer.server.ServerUtil.getParameter
import io.netty.handler.codec.http.QueryStringDecoder
import org.apache.commons.io.IOUtils
import java.io.File

@Throws(RequestHandlingException::class)
fun getFileName(decoder: QueryStringDecoder): String {
    return getParameter(decoder, "file")
            ?: throw RequestHandlingException("File is not specified. Uri: ${decoder.uri()}")
}

@Throws(RequestHandlingException::class)
fun getFilter(urlDecoder: QueryStringDecoder, logger: FlameLogger): Filter? {
    if (urlDecoder.parameters().containsKey("include") || urlDecoder.parameters().containsKey("exclude")) {
        val includingConfigsString = getParameter(urlDecoder, "include")
        val excludingConfigsString = getParameter(urlDecoder, "exclude")
        return Filter(includingConfigsString, excludingConfigsString, logger)
    }
    return null
}

typealias FindFile = (name: String) -> File?
typealias FindResource = (uri: String) -> ByteArray?

object CoreUtil {
    val findResource: FindResource = { uri ->
        try {
            CoreUtil::class.java.getResourceAsStream("/static/$uri").use { stream ->
                IOUtils.toByteArray(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


package com.github.korniloval.flameviewer

import com.github.korniloval.flameviewer.converters.trees.IntellijToTreesSetConverterFactory
import com.github.korniloval.flameviewer.converters.trees.TreeType.BACK_TRACES
import com.github.korniloval.flameviewer.converters.trees.TreeType.CALL_TRACES
import com.github.korniloval.flameviewer.handlers.*
import com.github.korniloval.flameviewer.server.*
import com.github.korniloval.flameviewer.server.handlers.*
import com.intellij.openapi.diagnostic.Logger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.QueryStringDecoder
import org.jetbrains.ide.HttpRequestHandler

class FlameViewerRequestHandler : HttpRequestHandler() {
    private val logger = LoggerAdapter(Logger.getInstance(FlameViewerRequestHandler::class.java))
    private val treeManager = TreeManager(IntellijToTreesSetConverterFactory)
    private val findFile: FindFile = { name -> PluginFileManager.getLogFile(name) }
    private val handler = DelegatingRequestHandler(
            mapOf(FILE_LIST to FileListHandler(logger),
                    HOT_SPOTS_JS_REQUEST to HotSpotsHandler(treeManager, logger, findFile),
                    CALL_TREE_JS_REQUEST to CallTreeHandler(treeManager, logger, findFile),
                    CALL_TREE_PREVIEW_JS_REQUEST to TreesPreviewHandler(treeManager, logger, findFile),
                    OUTGOING_CALLS_JS_REQUEST to CallTracesHandler(treeManager, logger, findFile),
                    INCOMING_CALLS_JS_REQUEST to BackTracesHandler(treeManager, logger, findFile),
                    CALL_TREE_COUNT to CallTreeCountMethods(treeManager, logger, findFile),
                    OUTGOING_CALLS_COUNT to AccumulativeTreesCountMethods(treeManager, CALL_TRACES, logger, findFile),
                    INCOMING_CALLS_COUNT to AccumulativeTreesCountMethods(treeManager, BACK_TRACES, logger, findFile),
                    DOES_FILE_EXIST to DoesFileExistHandler,

                    CALL_TREE to HtmlHandler,
                    OUTGOING_CALLS_FULL to HtmlHandler,
                    INCOMING_CALLS_FULL to HtmlHandler,
                    HOT_SPOTS to HtmlHandler,

                    CONNECTION_ALIVE to ConnectionAliveHandler(treeManager),
                    UPLOAD_FILE to PostFileHandler(FileUploader(), logger),
                    DELETE_FILE to DeleteFileHandler(PluginFileManager, logger),
                    UNDO_DELETE_FILE to UndoDeleteFileHandler(PluginFileManager, logger)),

            listOf(CSS_PATTERN to StaticHandler("text/css"),
                    JS_PATTERN to StaticHandler("text/javascript"),
                    FONT_PATTERN to StaticHandler("application/octet-stream"),
                    PNG_PATTERN to StaticHandler("image/png")))

    override fun process(urlDecoder: QueryStringDecoder, request: FullHttpRequest, context: ChannelHandlerContext): Boolean {
        return handler.process(request, context)
    }

    override fun isSupported(request: FullHttpRequest): Boolean {
        return request.method() == HttpMethod.POST || request.method() == HttpMethod.GET
    }
}

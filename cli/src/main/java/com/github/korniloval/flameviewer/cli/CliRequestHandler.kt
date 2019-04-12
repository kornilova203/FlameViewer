package com.github.korniloval.flameviewer.cli

import com.github.korniloval.flameviewer.converters.calltraces.ToCallTracesConverterFactoryCli
import com.github.korniloval.flameviewer.converters.calltree.ToCallTreeConverterFactoryCli
import com.github.korniloval.flameviewer.converters.trees.ToTreesSetConverterFactory
import com.github.korniloval.flameviewer.converters.trees.TreeType
import com.github.korniloval.flameviewer.server.*
import com.github.korniloval.flameviewer.server.handlers.*
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus

class CliRequestHandler : SimpleChannelInboundHandler<HttpRequest>() {

    private val logger = CliLogger()
    private val treeManager = TreeManager(ToTreesSetConverterFactory(ToCallTreeConverterFactoryCli, ToCallTracesConverterFactoryCli))
    private val findFile = cliFindFile
    private val handler = DelegatingRequestHandler(
            mapOf(FILE_LIST to CliFileListHandler(),
                    HOT_SPOTS_JSON to HotSpotsHandler(treeManager, logger, findFile),
                    SERIALIZED_CALL_TREE to CallTreeHandler(treeManager, logger, findFile),
                    SERIALIZED_CALL_TRACES to CallTracesHandler(treeManager, logger, findFile),
                    SERIALIZED_BACK_TRACES to BackTracesHandler(treeManager, logger, findFile),
                    CALL_TREE_PREVIEW to TreesPreviewHandler(treeManager, logger, findFile),
                    CALL_TREE_COUNT to CallTreeCountMethods(treeManager, logger, findFile),
                    CALL_TRACES_COUNT to AccumulativeTreesCountMethods(treeManager, TreeType.CALL_TRACES, logger, findFile),
                    BACK_TRACES_COUNT to AccumulativeTreesCountMethods(treeManager, TreeType.BACK_TRACES, logger, findFile),
                    DOES_FILE_EXIST to DoesFileExistHandler(findFile),

                    CALL_TREE_PAGE to HtmlHandler,
                    CALL_TRACES_PAGE to HtmlHandler,
                    BACK_TRACES_PAGE to HtmlHandler,
                    HOT_SPOTS_PAGE to HtmlHandler),

            listOf(CSS_PATTERN to StaticHandler("text/css"),
                    JS_PATTERN to StaticHandler("text/javascript"),
                    FONT_PATTERN to StaticHandler("application/octet-stream"),
                    PNG_PATTERN to StaticHandler("image/png")))


    override fun channelRead0(ctx: ChannelHandlerContext, request: HttpRequest) {
        if (!handler.process(request, ctx)) {
            ServerUtil.sendStatus(HttpResponseStatus.NOT_FOUND, ctx.channel())
        }
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        ctx.flush()
    }
}

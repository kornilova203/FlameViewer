package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;
import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendJson;

public class HotSpotsRequestHandler extends TreeRequestHandler {
    public HotSpotsRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context);
    }

    @Override
    public void process() {
        if (logFile == null) {
            return;
        }
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null ||
                fileName == null) {
            return;
        }
        sendJson(context, new Gson().toJson(TreeManager.getInstance().getHotSpots(logFile)));
    }
}

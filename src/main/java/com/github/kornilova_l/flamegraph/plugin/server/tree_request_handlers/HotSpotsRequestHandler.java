package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreesSet.HotSpot;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;
import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendJson;

public class HotSpotsRequestHandler extends TreeRequestHandler {
    public HotSpotsRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context);
    }

    @Override
    public void doActualProcess() {
        if (logFile == null) {
            return;
        }
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null || fileName == null) {
            return;
        }
        List<HotSpot> hotSpots = TreeManager.getInstance().getHotSpots(logFile);
        if (hotSpots == null) {
            sendJson(context, "{}"); // send empty object
        } else {
            sendJson(context, new Gson().toJson(hotSpots));
        }
    }
}

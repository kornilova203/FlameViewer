package com.github.kornilova_l.flamegraph.plugin.server.tree_request_handlers;

import com.github.kornilova_l.flamegraph.plugin.server.ServerNames;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.getParameter;
import static com.github.kornilova_l.flamegraph.plugin.server.ProfilerHttpRequestHandler.sendProto;

public class CallTreeRequestHandler extends TreeRequestHandler {

    public CallTreeRequestHandler(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        super(urlDecoder, context);
    }

    @Override
    public void process() {
        if (logFile == null) {
            return;
        }
        String uri = urlDecoder.path(); // without get parameters
        switch (uri) {
            case ServerNames.CALL_TREE_JS_REQUEST:
                sendProto(this.context,
                        TreeManager.getInstance().getCallTree(logFile, filter, getThreadsIds(urlDecoder)));
                return;
            case ServerNames.CALL_TREE_PREVIEW_JS_REQUEST:
                sendProto(context, TreeManager.getInstance().getCallTreesPreview(logFile, filter));
        }
    }

    @Nullable
    private List<Integer> getThreadsIds(QueryStringDecoder urlDecoder) {
        if (getParameter(urlDecoder, "threads") == null) {
            return null;
        }
        return urlDecoder.parameters().get("threads").stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}

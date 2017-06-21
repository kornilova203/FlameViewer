package com.github.kornilova_l.server;

import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.TreeConstructor;
import com.intellij.openapi.util.io.BufferExposingByteArrayOutputStream;
import com.intellij.openapi.util.io.StreamUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.RestService;
import org.jetbrains.io.Responses;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public class ProfilerRestService extends RestService {

    @NotNull
    @Override
    protected String getServiceName() {
        return ServerNames.NAME;
    }

    @Override
    protected boolean isMethodSupported(@NotNull HttpMethod method) {
        return method == HttpMethod.GET;
    }

    @Override
    protected boolean isPrefixlessAllowed() {
        return true;
    }

    @Override
    protected boolean isHostTrusted(@NotNull FullHttpRequest request) throws InterruptedException, InvocationTargetException {
        return true;
    }

    @Nullable
    @Override
    public String execute(@NotNull QueryStringDecoder urlDecoder,
                          @NotNull FullHttpRequest request,
                          @NotNull ChannelHandlerContext context) throws IOException {
        String uri = urlDecoder.uri();
        LOG.info("Request: " + uri);
        switch (uri) {
            case ServerNames.RESULTS:
                sendStatic(request, context, ServerNames.MAIN_NAME + "/index.html", "text/html");
                break;
            case ServerNames.ORIGINAL_TREE:
                TreesProtos.Trees trees = constructTimeTrees();
                sendTrees(request, context, trees);
                break;
            default:
                if (ServerNames.CSS_PATTERN.matcher(uri).matches()) {
                    sendStatic(request, context, uri, "text/css");
                } else if (ServerNames.JS_PATTERN.matcher(uri).matches()) {
                    sendStatic(request, context, uri, "text/javascript");
                } else if (ServerNames.FONT_PATTERN.matcher(uri).matches()) {
                    sendStatic(request, context, uri, "application/octet-stream");
                } else {
                    return "Not Found";
                }
        }
        return null;
    }

    private static void sendTrees(FullHttpRequest request,
                                  ChannelHandlerContext context,
                                  TreesProtos.Trees trees) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            trees.writeTo(outputStream);
            HttpResponse response = Responses.response(
                    "application/octet-stream",
                    Unpooled.wrappedBuffer(outputStream.toByteArray())
            );
            Responses.addNoCache(response);
            Responses.send(response, context.channel(), request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TreesProtos.Trees constructTimeTrees() {
        TreeConstructor treeConstructor = new TreeConstructor(
                new File("/home/lk/java-profiling-plugin/out/events64.ser")
        );
        return treeConstructor.constructOriginalTrees();
    }

    private void sendStatic(FullHttpRequest request,
                            ChannelHandlerContext context,
                            String fileName,
                            String contentType) throws IOException {
        try (
                BufferExposingByteArrayOutputStream byteOut = new BufferExposingByteArrayOutputStream();
                InputStream pageStream = getClass().getResourceAsStream(fileName)
        ) {
            byteOut.write(StreamUtil.loadFromStream(pageStream));
            HttpResponse response = Responses.response(
                    contentType,
                    Unpooled.wrappedBuffer(byteOut.getInternalBuffer())
            );
            Responses.addNoCache(response);
            Responses.send(response, context.channel(), request);
        }
    }
}

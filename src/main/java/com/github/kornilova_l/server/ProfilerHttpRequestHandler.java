package com.github.kornilova_l.server;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.TreeManager;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.ide.HttpRequestHandler;

import java.io.*;

public class ProfilerHttpRequestHandler extends HttpRequestHandler {

    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(ProfilerHttpRequestHandler.class);
    private final TreeManager treeManager = new TreeManager();

    @Override
    public boolean process(QueryStringDecoder urlDecoder,
                           FullHttpRequest fullHttpRequest,
                           ChannelHandlerContext context) throws IOException {
        String uri = urlDecoder.path(); // without get parameters
        LOG.info("Request: " + uri);
        switch (uri) {
            case ServerNames.CALL_TREE:
                LOG.info("call-tree.html");
                assert (urlDecoder.parameters().containsKey("file"));
                sendBytes(
                        context,
                        "text/html",
                        renderPage(
                                ServerNames.MAIN_NAME + "/call-tree.html",
                                urlDecoder.parameters().get("file").get(0)
                        )
                );
                break;
            case ServerNames.SELECT_FILE:
                LOG.info("select-file.html");
                sendStatic(context, ServerNames.MAIN_NAME + "/select-file.html", "text/html");
                break;
            case ServerNames.FILE_LIST:
                LOG.info("file list");
                sendFileList(context);
                break;
            case ServerNames.OUTGOING_CALLS:
                LOG.info("outgoing-calls.html");
                sendBytes(
                        context,
                        "text/html",
                        renderPage(
                                ServerNames.MAIN_NAME + "/outgoing-calls.html",
                                urlDecoder.parameters().get("file").get(0)
                        )
                );
                break;
            case ServerNames.INCOMING_CALLS:
                LOG.info("incoming-calls.html");
                sendBytes(
                        context,
                        "text/html",
                        renderPage(
                                ServerNames.MAIN_NAME + "/incoming-calls.html",
                                urlDecoder.parameters().get("file").get(0)
                        )
                );
                break;
            case ServerNames.CALL_TREE_JS_REQUEST:
                LOG.info("CALL_TREE_JS_REQUEST");
                sendTrees(context, treeManager.getCallTree());
                break;
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                LOG.info("OUTGOING_CALLS_JS_REQUEST");
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getOutgoingCalls(urlDecoder.parameters()));
                } else {
                    sendTree(context, treeManager.getOutgoingCalls());
                }
                break;
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
                LOG.info("INCOMING_CALLS_JS_REQUEST");
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getIncomingCalls(urlDecoder.parameters()));
                } else {
                    sendTree(context, treeManager.getIncomingCalls());
                }
                break;
            default:
                if (ServerNames.CSS_PATTERN.matcher(uri).matches()) {
                    LOG.info("CSS");
                    sendStatic(context, uri, "text/css");
                } else if (ServerNames.JS_PATTERN.matcher(uri).matches()) {
                    LOG.info("JS");
                    sendStatic(context, uri, "text/javascript");
                } else if (ServerNames.FONT_PATTERN.matcher(uri).matches()) {
                    sendStatic(context, uri, "application/octet-stream");
                } else if (ServerNames.PNG_PATTERN.matcher(uri).matches()) {
                    sendStatic(context, uri, "image/png");
                } else {
                    return false;
                }
        }
        return true;
    }

    private static byte[] renderPage(String htmlFile, String logFile) {
        htmlFile = htmlFile.replaceFirst("/[^/]+/", ProfilerFileManager.getStaticDir().getAbsolutePath() + "/");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(htmlFile)))) {
            return String.join("", bufferedReader.lines()
                    .map((line) -> line.replaceAll("\\{\\{ *fileName *\\}\\}", logFile)) // {{ fileName }}
                    .toArray(String[]::new)).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sendFileList(ChannelHandlerContext context) {
        String json = new Gson().toJson(
                ProfilerFileManager.getFileNameList()
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(json.getBytes());
            sendBytes(context, "application/json", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void sendTrees(ChannelHandlerContext context,
                                  TreesProtos.Trees trees) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            trees.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void sendTree(ChannelHandlerContext context,
                                 TreeProtos.Tree tree) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            tree.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void sendStatic(ChannelHandlerContext context,
                                   String fileName,
                                   String contentType) throws IOException {
        LOG.info("Got filename: " + fileName);
        String filePath = fileName.replaceFirst("/[^/]+/", ProfilerFileManager.getStaticDir().getAbsolutePath() + "/");
        LOG.info("This file will be sent: " + filePath);
        try (
                InputStream inputStream = new FileInputStream(
                        new File(filePath)
                )
        ) {
            sendBytes(context, contentType, IOUtils.toByteArray(inputStream));
        }
    }

    private static void sendBytes(ChannelHandlerContext context,
                                  String contentType,
                                  byte[] bytes) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(bytes)
        );
        response.headers().set("Content-Type", contentType);
        ChannelFuture f = context.channel().writeAndFlush(response);
        f.addListener(ChannelFutureListener.CLOSE);
    }
}

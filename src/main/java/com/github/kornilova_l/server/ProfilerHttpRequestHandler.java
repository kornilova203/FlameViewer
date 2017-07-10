package com.github.kornilova_l.server;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.protos.TreeProtos;
import com.github.kornilova_l.protos.TreesProtos;
import com.github.kornilova_l.server.trees.TreeManager;
import com.google.gson.Gson;
import com.intellij.openapi.application.PathManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.Responses;

import java.io.*;
import java.util.Objects;

public class ProfilerHttpRequestHandler extends HttpRequestHandler {

    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(ProfilerHttpRequestHandler.class);
    private final ProfilerFileManager fileManager = new ProfilerFileManager(PathManager.getSystemPath());
    private final TreeManager treeManager = new TreeManager(fileManager);

    private byte[] renderPage(String htmlFile, String logFile) {
        htmlFile = htmlFile.replaceFirst("/[^/]+/", fileManager.getStaticDir().getAbsolutePath() + "/");
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(htmlFile)))) {
            return String.join("", bufferedReader.lines()
                    .map((line) -> line.replaceAll("\\{\\{ *fileName *}}", logFile)) // {{ fileName }}
                    .toArray(String[]::new)).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendFileList(ChannelHandlerContext context) {
        String json = new Gson().toJson(
                fileManager.getFileNameList()
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
                                  @Nullable TreesProtos.Trees trees) {
        if (trees == null) {
            sendBytes(context, "application/octet-stream", new byte[0]);
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            trees.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void sendTree(ChannelHandlerContext context,
                                 @Nullable TreeProtos.Tree tree) {
        if (tree == null) {
            sendBytes(context, "application/octet-stream", new byte[0]);
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            tree.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void sendStatic(ChannelHandlerContext context,
                                   String fileName,
                                   String contentType) throws IOException {
        LOG.info("Got filename: " + fileName);
        String filePath = fileName.replaceFirst("/[^/]+/", fileManager.getStaticDir().getAbsolutePath() + "/");
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

    private static boolean processPostMethod(QueryStringDecoder urlDecoder, FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String uri = urlDecoder.path(); // without get parameters
        if (Objects.equals(uri, ServerNames.UPLOAD_FILE)) {
            String fileName = fullHttpRequest.headers().get("File-Name");
            LOG.info("Got file: " + fileName);
            if (supportedExtension(fileName)) {
//                ProfilerFileManager.saveFile(fullHttpRequest.content(), fileName);
                sendStatus(HttpResponseStatus.OK, context.channel());
                return true;
            }
        }
        return false;
    }

    private static void sendStatus(HttpResponseStatus status, Channel channel) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        HttpUtil.setContentLength(response, 0);
        Responses.addCommonHeaders(response);
        Responses.addNoCache(response);
        response.headers().set("X-Frame-Options", "Deny");
        Responses.send(response, channel, true);
    }

    private static boolean supportedExtension(String fileName) {
        String extension = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
        return Objects.equals(extension, "jfr") ||
                Objects.equals(extension, "ser");
    }

    @Override
    public boolean process(QueryStringDecoder urlDecoder,
                           FullHttpRequest fullHttpRequest,
                           ChannelHandlerContext context) {
        LOG.info(fullHttpRequest.method() + " Request: " + urlDecoder.uri());
        if (fullHttpRequest.method() == HttpMethod.POST) {
            return processPostMethod(urlDecoder, fullHttpRequest, context);
        } else {
            return processGetMethod(urlDecoder, context);
        }
    }

    @Override
    public boolean isSupported(FullHttpRequest request) {
        return request.method() == HttpMethod.POST ||
                request.method() == HttpMethod.GET;
    }

    private boolean processGetMethod(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        String uri = urlDecoder.path(); // without get parameters
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
                try {
                    sendStatic(context, ServerNames.MAIN_NAME + "/select-file.html", "text/html");
                } catch (IOException e) {
                    return false;
                }
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
                assert (urlDecoder.parameters().containsKey("file"));
                sendTrees(context, treeManager.getCallTree(urlDecoder.parameters().get("file").get(0)));
                break;
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                LOG.info("OUTGOING_CALLS_JS_REQUEST");
                assert (urlDecoder.parameters().containsKey("file"));
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getOutgoingCalls(urlDecoder.parameters()));
                } else {
                    sendTree(context, treeManager.getOutgoingCalls(urlDecoder.parameters().get("file").get(0)));
                }
                break;
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
                LOG.info("INCOMING_CALLS_JS_REQUEST");
                assert (urlDecoder.parameters().containsKey("file"));
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getIncomingCalls(urlDecoder.parameters()));
                } else {
                    sendTree(context, treeManager.getIncomingCalls(urlDecoder.parameters().get("file").get(0)));
                }
                break;
            default:
                try {
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
                } catch (IOException e) {
                    return false;
                }
        }
        return true;
    }
}

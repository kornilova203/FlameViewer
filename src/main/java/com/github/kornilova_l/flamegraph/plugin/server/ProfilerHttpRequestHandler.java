package com.github.kornilova_l.flamegraph.plugin.server;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.google.gson.Gson;
import com.intellij.openapi.application.PathManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.Responses;

import java.io.*;
import java.util.Objects;

public class ProfilerHttpRequestHandler extends HttpRequestHandler {

    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(ProfilerHttpRequestHandler.class);
    private final PluginFileManager fileManager = new PluginFileManager(PathManager.getSystemPath());
    private final TreeManager treeManager = new TreeManager(fileManager);

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

    private static void sendStatus(HttpResponseStatus status, Channel channel) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        HttpUtil.setContentLength(response, 0);
        Responses.addCommonHeaders(response);
        Responses.addNoCache(response);
        response.headers().set("X-Frame-Options", "Deny");
        Responses.send(response, channel, true);
    }

    @NotNull
    private static Extension getExtension(@NotNull String fileName) {
        String extension = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
        switch (extension) {
            case "jfr":
                return Extension.JFR;
            case "ser":
                return Extension.SER;
            default:
                return Extension.UNSUPPORTED;
        }
    }

    private byte[] renderPage(String htmlFilePath,
                              @NotNull String fileName,
                              @NotNull String projectName) {
        htmlFilePath = fileManager.getStaticFilePath(htmlFilePath);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(htmlFilePath)))) {
            return String.join("", bufferedReader.lines()
                    .map((line) -> line
                            .replaceAll("\\{\\{ *fileName *}}", fileName) // {{ fileName }}
                            .replaceAll("\\{\\{ *projectName *}}", projectName)
                    )
                    .toArray(String[]::new)).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendFileList(ChannelHandlerContext context, String projectName) {
        String json = new Gson().toJson(
                fileManager.getFileNameList(projectName)
        );
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(json.getBytes());
            sendBytes(context, "application/json", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void sendStatic(ChannelHandlerContext context,
                            String fileUri,
                            String contentType) throws IOException {
        LOG.info("Got filename: " + fileUri);
        String filePath = fileManager.getStaticFilePath(fileUri);
        LOG.info("This file will be sent: " + filePath);
        try (
                InputStream inputStream = new FileInputStream(
                        new File(filePath)
                )
        ) {
            sendBytes(context, contentType, IOUtils.toByteArray(inputStream));
        }
    }

    private boolean processPostMethod(QueryStringDecoder urlDecoder, FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String uri = urlDecoder.path(); // without get parameters
        if (Objects.equals(uri, ServerNames.UPLOAD_FILE)) {
            String fileName = fullHttpRequest.headers().get("File-Name");
            LOG.info("Got file: " + fileName);
            if (getExtension(fileName) != Extension.UNSUPPORTED) {
                fileManager.saveFile(fullHttpRequest.content(), fileName);
                sendStatus(HttpResponseStatus.OK, context.channel());
                return true;
            }
        }
        return false;
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
            case ServerNames.FILE_LIST:
                LOG.info("file list");
                if (urlDecoder.parameters().containsKey("project")) {
                    sendFileList(context, urlDecoder.parameters().get("project").get(0));
                }
                return true;
            case ServerNames.SELECT_FILE:
                LOG.info("select-file.html");
                try {
                    sendStatic(context, ServerNames.MAIN_NAME + "/select-file.html", "text/html");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
        }
        switch (uri) {
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
            case ServerNames.CALL_TREE_JS_REQUEST:
                processTreeRequest(uri, urlDecoder, context);
                return true;
        }
        switch (uri) {
            case ServerNames.CALL_TREE:
            case ServerNames.OUTGOING_CALLS:
            case ServerNames.INCOMING_CALLS:
                processHtmlRequest(uri, urlDecoder, context);
                return true;
        }
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
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void processHtmlRequest(String uri, QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        @Nullable String projectName = null;
        @Nullable String fileName = null;
        if (urlDecoder.parameters().containsKey("file") && urlDecoder.parameters().containsKey("project")) {
            projectName = urlDecoder.parameters().get("project").get(0);
            fileName = urlDecoder.parameters().get("file").get(0);
        }
        if (projectName == null || fileName == null) {
            return;
        }
        switch (uri) {
            case ServerNames.CALL_TREE:
                LOG.info("call-tree.html");
                assert (urlDecoder.parameters().containsKey("file"));
                sendBytes(
                        context,
                        "text/html",
                        renderPage(
                                ServerNames.MAIN_NAME + "/call-tree.html",
                                fileName,
                                projectName
                        )
                );
                break;
            case ServerNames.OUTGOING_CALLS:
                LOG.info("outgoing-calls.html");
                sendBytes(
                        context,
                        "text/html",
                        renderPage(
                                ServerNames.MAIN_NAME + "/outgoing-calls.html",
                                fileName,
                                projectName
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
                                fileName,
                                projectName
                        )
                );
                break;
        }
    }

    private void processTreeRequest(String uri, QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        @Nullable File logFile = null;
        if (urlDecoder.parameters().containsKey("file") && urlDecoder.parameters().containsKey("project")) {
            String projectName = urlDecoder.parameters().get("project").get(0);
            String fileName = urlDecoder.parameters().get("file").get(0);
            logFile = fileManager.getConfigFile(projectName, fileName);
        }
        if (logFile == null) {
            return;
        }
        switch (uri) {
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                LOG.info("OUTGOING_CALLS_JS_REQUEST");
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getOutgoingCalls(urlDecoder.parameters(), logFile));
                } else {
                    sendTree(context, treeManager.getOutgoingCalls(logFile));
                }
                break;
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
                LOG.info("INCOMING_CALLS_JS_REQUEST");
                if (urlDecoder.parameters().containsKey("method")) {
                    sendTree(context, treeManager.getIncomingCalls(urlDecoder.parameters(), logFile));
                } else {
                    sendTree(context, treeManager.getIncomingCalls(logFile));
                }
                break;
            case ServerNames.CALL_TREE_JS_REQUEST:
                LOG.info("CALL_TREE_JS_REQUEST");
                sendTrees(context, treeManager.getCallTree(logFile));
                break;
        }

    }

    enum Extension {
        JFR,
        SER,
        UNSUPPORTED
    }
}

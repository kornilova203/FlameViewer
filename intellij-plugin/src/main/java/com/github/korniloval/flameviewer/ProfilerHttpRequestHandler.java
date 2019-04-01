package com.github.korniloval.flameviewer;

import com.github.kornilova_l.libs.com.google.protobuf.Message;
import com.github.korniloval.flameviewer.converters.trees.Filter;
import com.github.korniloval.flameviewer.converters.trees.IntellijToTreesSetConverterFactory;
import com.github.korniloval.flameviewer.converters.trees.TreeType;
import com.github.korniloval.flameviewer.handlers.methods.count.AccumulativeTreesMethodCounter;
import com.github.korniloval.flameviewer.handlers.methods.count.CallTreeMethodsCounter;
import com.github.korniloval.flameviewer.handlers.tree.request.json.HotSpotsRequestHandler;
import com.github.korniloval.flameviewer.handlers.tree.request.tree.BackTracesRequestHandler;
import com.github.korniloval.flameviewer.handlers.tree.request.tree.CallTracesRequestHandler;
import com.github.korniloval.flameviewer.handlers.tree.request.trees.CallTreeRequestHandler;
import com.github.korniloval.flameviewer.handlers.tree.request.trees.TreesPreviewHandler;
import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import kotlin.Unit;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.Responses;

import java.io.*;
import java.util.Map;

public class ProfilerHttpRequestHandler extends HttpRequestHandler {

    private static final Logger LOG = Logger.getInstance(ProfilerHttpRequestHandler.class);
    private final PluginFileManager fileManager = PluginFileManager.INSTANCE;
    private final TreeManager treeManager = new TreeManager(IntellijToTreesSetConverterFactory.INSTANCE);
    private final FileUploader fileUploader = new FileUploader();

    public static void sendProto(ChannelHandlerContext context,
                                 @Nullable Message message) {
        if (message == null) {
            sendBytes(context, "application/octet-stream", new byte[0]);
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            message.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private static void sendBytes(ChannelHandlerContext context,
                                  String contentType,
                                  @NotNull byte[] bytes) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(bytes)
        );
        response.headers().set("Content-Type", contentType);
        ChannelFuture f = context.channel().writeAndFlush(response);
        f.addListener(ChannelFutureListener.CLOSE);
    }

    public static void sendStatus(HttpResponseStatus status, Channel channel) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        HttpUtil.setContentLength(response, 0);
        doSendStatus(response, channel);
    }

    public static void sendStatus(HttpResponseStatus status, Channel channel, String message) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(message.getBytes()));
        HttpUtil.setContentLength(response, message.getBytes().length);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        doSendStatus(response, channel);
    }

    private static void doSendStatus(DefaultFullHttpResponse response, Channel channel) {
        Responses.addNoCache(response);
        Responses.send(response, channel, true);
    }

    @Nullable
    public static String getParameter(QueryStringDecoder urlDecoder, String key) {
        Map<String, java.util.List<String>> parameters = urlDecoder.parameters();
        if (parameters.containsKey(key)) {
            return parameters.get(key).get(0);
        }
        return null;
    }

    public static void sendJson(ChannelHandlerContext context, @NotNull String json) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(json.getBytes());
            sendBytes(context, "application/json", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    @Nullable
    public static Filter getFilter(QueryStringDecoder urlDecoder) {
        if (urlDecoder.parameters().containsKey("include") || urlDecoder.parameters().containsKey("exclude")) {
            String includingConfigsString = getParameter(urlDecoder, "include");
            String excludingConfigsString = getParameter(urlDecoder, "exclude");
            return new Filter(includingConfigsString, excludingConfigsString, (e) -> { LOG.error(e); return Unit.INSTANCE; });
        }
        return null;
    }

    @NotNull
    private byte[] renderPage(String htmlFilePath,
                              @Nullable String fileName,
                              @NotNull String projectName,
                              @Nullable String include,
                              @Nullable String exclude) {
        File staticFile = fileManager.getStaticFile(htmlFilePath);
        if (staticFile == null) {
            throw new RuntimeException("Cannot render page " + htmlFilePath + " project: " +
                    projectName + " file " + fileName + " include " + include + " exclude " + exclude);
        }
        String filterParameters = getFilterAsGetParameters(include, exclude);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(staticFile))) {
            return String.join("", bufferedReader.lines()
                    .map((line) -> {
                                String replacement = fileName == null ?
                                        "" :
                                        "file=" + fileName + "&";
                                return line.replace("{{ projectName }}", projectName)
                                        .replace("{{ fileParam }}", replacement)
                                        .replace("{{ filter }}", filterParameters);
                            }
                    )
                    .toArray(String[]::new)).getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Cannot render page " + htmlFilePath + " project: " +
                    projectName + " file " + fileName + " include " + include + " exclude " + exclude, e);
        }
    }

    private String getFilterAsGetParameters(@Nullable String include, @Nullable String exclude) {
        String res = "";
        if (include != null) {
            res += "&include=" + include;
        }
        if (exclude != null) {
            res += "&exclude=" + exclude;
        }
        return res;
    }

    private void sendFileList(ChannelHandlerContext context, String projectName) {
        sendJson(context, new Gson().toJson(fileManager.getFileNameList(projectName)));
    }

    private void sendListProjects(ChannelHandlerContext context) {
        String json = new Gson().toJson(fileManager.getProjectList());
        sendJson(context, json);
    }

    private void sendStatic(ChannelHandlerContext context,
                            String fileUri,
                            String contentType) throws IOException {
        File staticFile = fileManager.getStaticFile(fileUri);
        if (staticFile == null) {
            throw new RuntimeException("Cannot find static files. File uri: " + fileUri);
        }
        try (InputStream inputStream = new FileInputStream(staticFile)) {
            sendBytes(context, contentType, IOUtils.toByteArray(inputStream));
        }
    }

    private boolean processPostMethod(QueryStringDecoder urlDecoder, FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String uri = urlDecoder.path(); // without get parameters
        switch (uri) {
            case ServerNames.CONNECTION_ALIVE:
                sendStatus(HttpResponseStatus.OK, context.channel());
                return true;
            case ServerNames.UPLOAD_FILE:
                uploadFile(fullHttpRequest, context);
                return true;
            case ServerNames.DELETE_FILE:
            case ServerNames.UNDO_DELETE_FILE:
                manageDelete(fullHttpRequest, context, uri);
                return true;
        }
        return false;
    }

    private void manageDelete(FullHttpRequest fullHttpRequest,
                              ChannelHandlerContext context,
                              String uri) {
        String fileName = fullHttpRequest.headers().get("File-Name");
        String projectName = fullHttpRequest.headers().get("Project-Name");
        if (fileName == null || projectName == null) {
            return;
        }
        switch (uri) {
            case ServerNames.DELETE_FILE:
                LOG.info("Delete file: " + fileName + " project: " + projectName);
                fileManager.deleteFile(fileName, projectName);
                break;
            case ServerNames.UNDO_DELETE_FILE:
                LOG.info("Undo delete file: " + fileName + " project: " + projectName);
                fileManager.undoDeleteFile(fileName, projectName);
        }
        sendStatus(HttpResponseStatus.OK, context.channel());
    }

    private synchronized void uploadFile(FullHttpRequest fullHttpRequest,
                                         ChannelHandlerContext context) {
        String fileName = fullHttpRequest.headers().get("File-Name");
        int[] fileParts = getFileParts(fullHttpRequest);
        if (fileParts == null) {
            sendStatus(HttpResponseStatus.BAD_REQUEST, context.channel(), "Request does not contain File-Part header");
            return;
        }
        int currentPart = fileParts[0];
        int totalPartsCount = fileParts[1];
        LOG.info("Got file: " + fileName);
        byte[] bytes = getBytes(fullHttpRequest.content());
        fileUploader.upload(fileName, bytes, currentPart, totalPartsCount);
        sendStatus(HttpResponseStatus.OK, context.channel(), "File part was successfully uploaded");
    }

    /**
     * @return information about what part was sent
     */
    private int[] getFileParts(FullHttpRequest fullHttpRequest) {
        String filePartsString = fullHttpRequest.headers().get("File-Part");
        String[] fileParts = filePartsString.split("/");
        if (fileParts.length != 2) {
            LOG.error("Header File-Parts does not contain information in format '<current part>/<total parts>'. " + filePartsString);
        }
        try {
            return new int[]{Integer.parseInt(fileParts[0]), Integer.parseInt(fileParts[1])};
        } catch (NumberFormatException e) {
            LOG.error("Header File-Parts does not contain information in format '<current part>/<total parts>'. " + filePartsString, e);
        }
        return null;
    }

    private byte[] getBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public boolean process(@NotNull QueryStringDecoder urlDecoder,
                           @NotNull FullHttpRequest fullHttpRequest,
                           @NotNull ChannelHandlerContext context) {
        if (!urlDecoder.uri().startsWith(ServerNames.MAIN_NAME)) {
            return false;
        }
        treeManager.updateLastTime();
        if (fullHttpRequest.method() == HttpMethod.POST) {
            return processPostMethod(urlDecoder, fullHttpRequest, context);
        } else {
            return processGetMethod(urlDecoder, context, fullHttpRequest);
        }
    }

    @Override
    public boolean isSupported(@NotNull FullHttpRequest request) {
        return request.method() == HttpMethod.POST ||
                request.method() == HttpMethod.GET;
    }

    private boolean processGetMethod(QueryStringDecoder urlDecoder, ChannelHandlerContext context, FullHttpRequest fullHttpRequest) {
        String uri = urlDecoder.path(); // without get parameters
        switch (uri) {
            case ServerNames.LIST_PROJECTS:
                sendListProjects(context);
                return true;
            case ServerNames.FILE_LIST:
                String project = getParameter(urlDecoder, "project");
                if (project != null) {
                    sendFileList(context, project);
                }
                return true;
            case ServerNames.HOT_SPOTS_JS_REQUEST:
                new HotSpotsRequestHandler(urlDecoder, context, treeManager).process();
                return true;
            case ServerNames.DOES_FILE_EXIST:
                sendIfFileExist(fullHttpRequest, context);
                return true;
        }
        switch (uri) {
            case ServerNames.CALL_TREE_JS_REQUEST:
                new CallTreeRequestHandler(urlDecoder, context, treeManager).process();
                return true;
            case ServerNames.CALL_TREE_PREVIEW_JS_REQUEST:
                new TreesPreviewHandler(urlDecoder, context, treeManager).process();
                return true;
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                new CallTracesRequestHandler(urlDecoder, context, treeManager).process();
                return true;
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
                new BackTracesRequestHandler(urlDecoder, context, treeManager).process();
                return true;
            case ServerNames.CALL_TREE_COUNT:
                new CallTreeMethodsCounter(urlDecoder, context, treeManager).sendJson();
                return true;
            case ServerNames.OUTGOING_CALLS_COUNT:
                new AccumulativeTreesMethodCounter(urlDecoder, context, TreeType.CALL_TRACES, treeManager).sendJson();
                return true;
            case ServerNames.INCOMING_CALLS_COUNT:
                new AccumulativeTreesMethodCounter(urlDecoder, context, TreeType.BACK_TRACES, treeManager).sendJson();
                return true;
        }
        switch (uri) {
            case ServerNames.CALL_TREE:
            case ServerNames.OUTGOING_CALLS:
            case ServerNames.INCOMING_CALLS:
            case ServerNames.HOT_SPOTS:
                processHtmlRequest(uri, context, urlDecoder);
                return true;
        }
        try {
            if (ServerNames.CSS_PATTERN.matcher(uri).matches()) {
                sendStatic(context, uri, "text/css");
            } else if (ServerNames.JS_PATTERN.matcher(uri).matches()) {
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

    /**
     * Sends code 302 FOUND if file was found
     * and code 404 NOT_FOUND otherwise.
     */
    private void sendIfFileExist(FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String fileName = fullHttpRequest.headers().get("File-Name");
        String projectName = fullHttpRequest.headers().get("Project-Name");
        if (fileManager.getLogFile(projectName, fileName) != null) {
            sendStatus(HttpResponseStatus.FOUND, context.channel(), "File " + fileName + " was found in uploaded messages");
        } else {
            sendStatus(HttpResponseStatus.NOT_FOUND, context.channel(), "File " + fileName + " was NOT found in uploaded messages");
        }
    }

    private void processHtmlRequest(String uri,
                                    ChannelHandlerContext context,
                                    QueryStringDecoder urlDecoder) {
        String projectName = getParameter(urlDecoder, "project");
        if (projectName == null) {
            projectName = "";
        }
        sendBytes(
                context,
                "text/html",
                renderPage(
                        uri + ".html",
                        getParameter(urlDecoder, "file"),
                        projectName,
                        getParameter(urlDecoder, "include"),
                        getParameter(urlDecoder, "exclude")
                )
        );
    }
}

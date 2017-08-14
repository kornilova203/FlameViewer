package com.github.kornilova_l.flamegraph.plugin.server;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.server.trees.TreeManager;
import com.github.kornilova_l.flamegraph.proto.TreeProtos;
import com.github.kornilova_l.flamegraph.proto.TreesProtos;
import com.google.gson.Gson;
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
import java.util.Map;
import java.util.Objects;

public class ProfilerHttpRequestHandler extends HttpRequestHandler {

    private static final com.intellij.openapi.diagnostic.Logger LOG =
            com.intellij.openapi.diagnostic.Logger.getInstance(ProfilerHttpRequestHandler.class);
    private final PluginFileManager fileManager = PluginFileManager.getInstance();
    private final TreeManager treeManager = new TreeManager();

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
    public static TreeManager.Extension getExtension(@NotNull String fileName) {
        String extension = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
        switch (extension) {
            case "jfr":
                return TreeManager.Extension.JFR;
            case "jfr.converted":
                return TreeManager.Extension.JFR_CONVERTED;
            case "ser":
                return TreeManager.Extension.SER;
            default:
                return TreeManager.Extension.UNSUPPORTED;
        }
    }

    @Nullable
    private static String getParameter(QueryStringDecoder urlDecoder, String key) {
        Map<String, java.util.List<String>> parameters = urlDecoder.parameters();
        if (parameters.containsKey(key)) {
            return parameters.get(key).get(0);
        }
        return null;
    }

    private byte[] renderPage(String htmlFilePath,
                              @Nullable String fileName,
                              @NotNull String projectName) {
        htmlFilePath = fileManager.getStaticFilePath(htmlFilePath);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(htmlFilePath)))) {
            return String.join("", bufferedReader.lines()
                    .map((line) -> {
                                String replacement = fileName == null ?
                                        "" :
                                        "file=" + fileName + "&";
                        line = line.replace(
                                "{{ fileParam }}", replacement);
                        return line.replace("{{ projectName }}", projectName);
                            }
                    )
                    .toArray(String[]::new)).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendFileList(ChannelHandlerContext context, String projectName) {
        sendJson(context, new Gson().toJson(fileManager.getFileNameList(projectName)));
    }

    private void sendListProjects(ChannelHandlerContext context) {
        String json = new Gson().toJson(
                fileManager.getProjectList()
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
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            sendBytes(context, contentType, IOUtils.toByteArray(inputStream));
        }
    }

    private boolean processPostMethod(QueryStringDecoder urlDecoder, FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String uri = urlDecoder.path(); // without get parameters
        switch (uri) {
            case ServerNames.UPLOAD_FILE:
                uploadFile(fullHttpRequest, context);
                return true;
            case ServerNames.DELETE_FILE:
                String fileName = fullHttpRequest.headers().get("File-Name");
                String projectName = fullHttpRequest.headers().get("Project-Name");
                if (fileName == null || projectName == null) {
                    return true;
                }
                LOG.info("Delete file: " + fileName + " project: " + projectName);
                fileManager.deleteFile(fileName, projectName);
                sendStatus(HttpResponseStatus.OK, context.channel());
                return true;
        }
        return false;
    }

    private void uploadFile(FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
        String fileName = fullHttpRequest.headers().get("File-Name");
        LOG.info("Got file: " + fileName);
        switch (getExtension(fileName)) {
            case JFR:
                fileManager.convertAndSave(fullHttpRequest.content(), fileName);
                break;
            case SER:
                fileManager.save(fullHttpRequest.content(), fileName);
                break;
            case UNSUPPORTED:
            default:
                sendStatus(HttpResponseStatus.BAD_REQUEST, context.channel());
                return;

        }
        sendStatus(HttpResponseStatus.OK, context.channel());
    }


    @Override
    public boolean process(QueryStringDecoder urlDecoder,
                           FullHttpRequest fullHttpRequest,
                           ChannelHandlerContext context) {
        LOG.info(fullHttpRequest.method() + " Request: " + urlDecoder.uri());
        if (!urlDecoder.uri().startsWith(ServerNames.MAIN_NAME)) {
            return false;
        }
        LOG.info("It is profiler request");
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
            case ServerNames.LIST_PROJECTS:
                LOG.info("list-projects");
                sendListProjects(context);
                return true;
            case ServerNames.FILE_LIST:
                LOG.info("file list");
                String project = getParameter(urlDecoder, "project");
                if (project != null) {
                    sendFileList(context, project);
                }
                return true;
            case ServerNames.HOT_SPOTS_JS_REQUEST:
                LOG.info("hot spots js request");
                sendHotSpots(urlDecoder, context);
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
            case ServerNames.HOT_SPOTS:
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

    private void sendHotSpots(QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null ||
                fileName == null) {
            return;
        }
        File logFile = fileManager.getConfigFile(projectName, fileName);
        if (logFile == null) {
            return;
        }
        sendJson(context, new Gson().toJson(treeManager.getHotSpots(logFile)));
    }

    private void sendJson(ChannelHandlerContext context, String json) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(json.getBytes());
            sendBytes(context, "application/json", outputStream.toByteArray());
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    private void processHtmlRequest(String uri, QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        @Nullable String projectName;
        @Nullable String fileName = null;
        if (urlDecoder.parameters().containsKey("project")) {
            projectName = urlDecoder.parameters().get("project").get(0);
        } else {
            return;
        }
        if (urlDecoder.parameters().containsKey("file")) {
            fileName = urlDecoder.parameters().get("file").get(0);
        }
        LOG.info(uri + ".html");
        sendBytes(
                context,
                "text/html",
                renderPage(
                        uri + ".html",
                        fileName,
                        projectName
                )
        );
    }

    private void processTreeRequest(String uri, QueryStringDecoder urlDecoder, ChannelHandlerContext context) {
        @Nullable File logFile = getLogFile(urlDecoder);
        if (logFile == null) {
            return;
        }
        Configuration configuration = null;
        if (urlDecoder.parameters().containsKey("include") || urlDecoder.parameters().containsKey("exclude")) {
            String includingConfigsString = getParameter(urlDecoder, "include");
            String[] includingConfigs = null;
            if (includingConfigsString != null) {
                includingConfigs = includingConfigsString.split(",");
            }
            String excludingConfigsString = getParameter(urlDecoder, "exclude");
            String[] excludingConfigs = null;
            if (excludingConfigsString != null) {
                excludingConfigs = excludingConfigsString.split(",");
            }
            configuration = PluginConfigManager.newConfiguration(includingConfigs, excludingConfigs);

        }
        switch (uri) {
            case ServerNames.OUTGOING_CALLS_JS_REQUEST:
            case ServerNames.INCOMING_CALLS_JS_REQUEST:
                processAccumulativeTreeRequest(uri, urlDecoder, context, logFile, configuration);
                break;
            case ServerNames.CALL_TREE_JS_REQUEST:
                LOG.info("CALL_TREE_JS_REQUEST");
                sendTrees(context, treeManager.getCallTree(logFile, configuration));
                break;
        }

    }

    @Nullable
    private File getLogFile(QueryStringDecoder urlDecoder) {
        String projectName = getParameter(urlDecoder, "project");
        String fileName = getParameter(urlDecoder, "file");
        if (projectName == null || fileName == null) {
            return null;
        }
        return fileManager.getConfigFile(projectName, fileName);
    }

    private void processAccumulativeTreeRequest(String uri,
                                                QueryStringDecoder urlDecoder,
                                                ChannelHandlerContext context,
                                                @Nullable File logFile, Configuration configuration) {
        String methodName = getParameter(urlDecoder, "method");
        String className = getParameter(urlDecoder, "class");
        String desc = getParameter(urlDecoder, "desc");
        String isStaticString = getParameter(urlDecoder, "isStatic");
        if (methodName != null && className != null && desc != null) {
            boolean isStatic;
            isStatic = isStaticString != null && Objects.equals(isStaticString, "true");
            switch (uri) {
                case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                    LOG.info("outgoing calls for method js request");
                    sendTree(context, treeManager.getTree(logFile, TreeManager.TreeType.OUTGOING_CALLS, className, methodName,
                            desc, isStatic, configuration));
                    return;
                case ServerNames.INCOMING_CALLS_JS_REQUEST:
                    LOG.info("incoming calls for method js request");
                    sendTree(context, treeManager.getTree(logFile, TreeManager.TreeType.INCOMING_CALLS, className, methodName,
                            desc, isStatic, configuration));
            }
        } else {
            switch (uri) {
                case ServerNames.OUTGOING_CALLS_JS_REQUEST:
                    LOG.info("OUTGOING_CALLS_JS_REQUEST");
                    sendTree(context, treeManager.getTree(logFile, TreeManager.TreeType.OUTGOING_CALLS, configuration));
                    return;
                case ServerNames.INCOMING_CALLS_JS_REQUEST:
                    sendTree(context, treeManager.getTree(logFile, TreeManager.TreeType.INCOMING_CALLS, configuration));
            }
        }
    }
}

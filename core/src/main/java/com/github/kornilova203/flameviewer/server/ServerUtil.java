package com.github.kornilova203.flameviewer.server;

import com.github.kornilova203.flameviewer.FlameLogger;
import com.github.kornilova_l.libs.com.google.protobuf.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ServerUtil {
    public static void sendProto(ChannelHandlerContext context,
                                 @Nullable Message message,
                                 @NotNull FlameLogger logger) {
        if (message == null) {
            sendBytes(context, "application/octet-stream", new byte[0]);
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            message.writeTo(outputStream);
            sendBytes(context, "application/octet-stream", outputStream.toByteArray());
        } catch (IOException e) {
            logger.error("Failed to send proto", e);
        }
    }

    public static void sendBytes(ChannelHandlerContext context,
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

    public static void sendStatus(HttpResponseStatus status, io.netty.channel.Channel channel, String message) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(message.getBytes()));
        HttpUtil.setContentLength(response, message.getBytes().length);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        doSendStatus(response, channel);
    }

    private static void doSendStatus(DefaultFullHttpResponse response, Channel channel) {
        response.headers().add(HttpHeaderNames.CACHE_CONTROL, "no-cache, no-store, must-revalidate, max-age=0");
        response.headers().add(HttpHeaderNames.PRAGMA, "no-cache");
        if (!channel.isActive()) return;
        ChannelFuture future = channel.write(response);
        channel.flush();
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Nullable
    public static String getParameter(QueryStringDecoder urlDecoder, String key) {
        Map<String, List<String>> parameters = urlDecoder.parameters();
        if (parameters.containsKey(key)) {
            return parameters.get(key).get(0);
        }
        return null;
    }

    public static void sendJson(ChannelHandlerContext context, @NotNull String json, @NotNull FlameLogger logger) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(json.getBytes());
            sendBytes(context, "application/json", outputStream.toByteArray());
        } catch (IOException e) {
            logger.error("Failed to send json data", e);
        }
    }

    @Nullable
    public static String getFileExtension(@NotNull String name) {
        int index = name.lastIndexOf('.');
        if (index < 0) return null;
        return name.substring(index + 1);
    }

    @Nullable
    public static Integer validateMaxNumOfVisibleNodes(@Nullable Integer maxNumOfVisibleNodes, @NotNull FlameLogger logger) {
        if (maxNumOfVisibleNodes == null) return null;
        if (maxNumOfVisibleNodes <= 0) {
            logger.warn("Maximum number of visible nodes must be bigger than 0. Actual value: " + maxNumOfVisibleNodes, null);
            return null;
        }
        return maxNumOfVisibleNodes;
    }
}

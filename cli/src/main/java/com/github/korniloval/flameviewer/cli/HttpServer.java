package com.github.korniloval.flameviewer.cli;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.BindException;

import static com.github.korniloval.flameviewer.cli.CliUtilKt.encodeFileName;
import static com.github.korniloval.flameviewer.server.ServerNamesKt.CALL_TRACES_PAGE;

class HttpServer {
    private static final int PORT = 8080;

    static void start(@NotNull File file) throws Throwable {
        tryStart(file, PORT);
    }

    private static void tryStart(File file, int port) throws InterruptedException, IOException {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(eventLoopGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer())
                    .channel(NioServerSocketChannel.class);

            Channel ch = bootstrap.bind(port).sync().channel();
            System.out.println("http://localhost:" + port + CALL_TRACES_PAGE + "?file=" + encodeFileName(file.getCanonicalPath()));
            ch.closeFuture().sync();
        } catch (BindException e) {
            tryStart(file, port + 1);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static class HttpServerInitializer extends ChannelInitializer<Channel> {

        @Override
        protected void initChannel(Channel ch) {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new CliRequestHandler());
        }
    }
}

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

import static com.github.korniloval.flameviewer.cli.CliUtilKt.encodeFileName;
import static com.github.korniloval.flameviewer.server.ServerNamesKt.CALL_TRACES_PAGE;

class HttpServer {
    private static final int PORT = 8080;

    static void start(@NotNull File file) throws Throwable {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(eventLoopGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer())
                    .channel(NioServerSocketChannel.class);

            Channel ch = bootstrap.bind(PORT).sync().channel();
            System.out.println("http://localhost:" + PORT + CALL_TRACES_PAGE + "?file=" + encodeFileName(file.getCanonicalPath()));
            ch.closeFuture().sync();
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

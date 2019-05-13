package com.filippov;

import com.filippov.Handlers.AuthHandler;
import com.filippov.Handlers.ObjectInboundHandler;
import com.filippov.Handlers.ServerChannelInitializer;
import com.filippov.HibernateUtils.HibernateSessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    ChannelFuture channelFuture;
    public static final Path rootPath = Paths.get("","ServerStorage").toAbsolutePath();

    public void run() throws Exception {
        System.out.println(rootPath.toAbsolutePath().toString());

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer())
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            channelFuture = bootstrap.bind("127.0.0.1", 8189).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    System.out.println("Сервер запущен на порту " + channelFuture.channel().localAddress().toString());
                }
            }).sync();

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}


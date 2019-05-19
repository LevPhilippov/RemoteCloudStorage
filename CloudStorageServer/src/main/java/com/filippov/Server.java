package com.filippov;

import com.filippov.Handlers.ServerChannelInitializer;
import com.filippov.HibernateUtils.HibernateSessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    ChannelFuture channelFuture;
    public static final Path rootPath = Paths.get("","ServerStorage");
    private static final Logger LOGGER = LogManager.getLogger(Server.class.getCanonicalName());

    public void run() throws Exception {
        LOGGER.info("Базовый путь к папке-хранилищу: " + rootPath.toString());

        if(!Files.exists(rootPath.toAbsolutePath())) {
            Files.createDirectories(rootPath);
        }

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
                    LOGGER.warn("Сервер запущен на порту: {}", channelFuture.channel().localAddress().toString());
                }
            }).sync();

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            HibernateSessionFactory.shutdown();

        }
    }
}


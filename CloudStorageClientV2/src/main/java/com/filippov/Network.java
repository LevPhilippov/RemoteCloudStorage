package com.filippov;

import com.filippov.Handlers.ClientAnswerHandler;
import com.filippov.Handlers.ObjectInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Network{
    private static Network ourInstance = new Network();
    public static Network getInstance() {return ourInstance;}
    private Network(){}
    private static PathHolder pathHolder = new PathHolder();;
    private ChannelFuture cf;
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
    private static Controller controller;

    public void startNetwork() {
        bossGroup = new NioEventLoopGroup(2);
        bootstrap = new Bootstrap();
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler("EndLogger", LogLevel.INFO),
                                    new ObjectDecoder(1024*1024*10,ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new ObjectInboundHandler(),
                                    new ClientAnswerHandler()
                            );
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);

                    cf = bootstrap.connect("127.0.0.1", 8189);
                    cf.addListener((ChannelFutureListener) channelFuture -> {
                        if(!channelFuture.isSuccess()) {
                            System.out.println("Connnection failed!");
                        } else {
                            System.out.println("Connection success!");
                            requestFilesList();
                        }

                    }).sync();
                    cf.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    shutdown();
                }
            }
        });
        networkThread.setDaemon(true);
        networkThread.start();
    }

    public void shutdown() {
        cf.channel().closeFuture();
        bossGroup.shutdownGracefully();
    }

    /**
     * Принимает на вход список имен файлов в директории, где находится клиент выполняет запись этих файлов в канал
     * */
    public void writeFilesIntoChannel(ObservableList<String> os, Request.RequestType requestType) {
            os.stream().map((s) -> Paths.get(pathHolder.getClientPath() + "/" + s)).forEach((path -> {
                if (Files.exists(path)) {
                    try {
                        Files.walkFileTree(path, new MyFileVisitor(pathHolder.getClientPath(), pathHolder.getServerPath(), cf.channel(), requestType)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }));
        System.out.println("В конце директория клиента: " + pathHolder.getClientPath());
        System.out.println("В конце директория сервера: " + pathHolder.getServerPath());
        requestFilesList();
    }

    /**
    * Метод отправляет запрос {@link Request} к серверу на получение списка файлов в указанной дидектории на сервере
     *
     * */

    public void requestFilesList() {
        System.out.println("Запрашиваю список файлов в каталоге " + pathHolder.getServerPath());
        cf.channel().writeAndFlush(new Request().setRequestType(Request.RequestType.FILELIST).setServerPath(pathHolder.getServerPath()));
    }

    public void sendRequest(ObservableList<String> os, Request.RequestType requestType) {
        List <File> filesList = new ArrayList<>();
        os.stream().filter(pathHolder.getServerPathMap()::containsKey).forEach((key) -> {
            filesList.add(pathHolder.getServerPathMap().get(key));
        });

        System.out.println("Запрос файлов из облака " + filesList);
        cf.channel().writeAndFlush(new Request()
                .setRequestType(requestType)
                .setServerPath(pathHolder.getServerPath())
                .setFileList(filesList)
                .setClientPath(pathHolder.getClientPath()));
    }

    public PathHolder getPathHolder() {
        return pathHolder;
    }

    public Controller getController() {
        return controller;
    }

    public static void setController(Controller controller) {
        Network.controller = controller;
    }

}
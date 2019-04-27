package com.filippov;

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
import java.nio.file.Files;
import java.nio.file.Paths;
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
//                                    new ClientOutboundHandler(),
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
                            requestFilesList(pathHolder.getServerPath());
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

    public ChannelFuture getCf() {
        return cf;
    }

    public void shutdown() {
        cf.channel().closeFuture();
        bossGroup.shutdownGracefully();
    }

    public void sendFileToCloud(ObservableList<String> os) {
        for (String o : os) {
//            Path path = Paths.get(pathHolder.getClientPath() + File.separator + o);
//            System.out.println(path.toString());
//            try {
//                CloudWrappedObject cwo =  CloudObjectWrapper.wrapFile(path, localPath, destPath);
//                cf.channel().writeAndFlush(cwo).addListener((ChannelFutureListener) channelFuture -> {
//                    System.out.println("Writing Complete!");
//                    requestFilesList(pathHolder.getServerPath());
//                });
//            } catch (IOException a) {
//                System.out.println("Ошибка записи");
//                a.printStackTrace();
//            }
//        }
            os.stream().map((s) -> Paths.get(pathHolder.getClientPath() + File.separator + s)).forEach((p -> {
                if (Files.exists(p)) {
                    System.out.println(p.toString());
                    try {
                        CloudWrappedObject cwo = CloudObjectWrapper.wrapFile(p, pathHolder.getClientPath(), pathHolder.getServerPath());
                        cf.channel().writeAndFlush(cwo).addListener((ChannelFutureListener) channelFuture -> {
                            System.out.println("Writing Complete!");
                            requestFilesList(pathHolder.getServerPath());
                        });
                    } catch (IOException a) {
                        System.out.println("Ошибка записи");
                        a.printStackTrace();
                    }
                }
            }));
        }
        requestFilesList(pathHolder.getServerPath());
    }

    public Controller getController() {
        return controller;
    }

    public static void setController(Controller controller) {
        Network.controller = controller;
    }

    /**
    * Метод отправляет запрос {@link Request} к серверу на получение списка файлов в указанной дидектории на сервере
     * @param path локальный адрес на сервере
     * */
    public void requestFilesList(String path) {
        System.out.println("Запрашиваю список файлов в каталоге " + path);
        cf.channel().writeAndFlush(new Request().setRequestType(Request.RequestType.FILELIST).setServerPath(path));
    }

    public void requestFile(ObservableList<String> os) {
        List <String> filesList = new ArrayList<>();
        filesList.addAll(os);
        System.out.println("Запрос файлов из облака " + filesList);
        cf.channel().writeAndFlush(new Request()
                .setRequestType(Request.RequestType.GETFILES)
                .setServerPath(pathHolder.getServerPath())
                .setFileList(filesList)
                .setClientPath(pathHolder.getClientPath()));
    }

    public PathHolder getPathHolder() {
        return pathHolder;
    }
}
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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    public void writeFilesIntoChannel(ObservableList<String> os) {
            os.stream().map((s) -> Paths.get(pathHolder.getClientPath() + "/" + s)).forEach((path -> {
                if (Files.exists(path)) {
                    try {
                        Files.walkFileTree(path, new MyFileVisitor(pathHolder.getClientPath(), pathHolder.getServerPath(), cf.channel())
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
        String path = pathHolder.getServerPath();
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

    public Controller getController() {
        return controller;
    }

    public static void setController(Controller controller) {
        Network.controller = controller;
    }

//заменено на общий MyFileVisitor и запись в канал в классе CloudWrappedObject;
//    Consumer<Path> consumer = p -> {
//
//            if (Files.exists(p)) {
//                    try {
//                        Files.walkFileTree(p, new MyFileVisitor()
//                                new FileVisitor<Path>() {
//                            @Override
//                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                                pathHolder.setClientPath(dir.toString());
//                                pathHolder.setServerPath(pathHolder.getServerPath() + "/" + dir.getFileName().toString());
//                                System.out.println("Зашли в директорию на клиенте: "+ pathHolder.getClientPath());
//                                System.out.println("Зашли в директорию на сервере: " + pathHolder.getServerPath());
//                                return FileVisitResult.CONTINUE;
//                            }
//
//                            @Override
//                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                                writeIntoChannel(file);
//                                return FileVisitResult.CONTINUE;
//                            }
//
//                            @Override
//                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                                System.out.println("Ошибка отправки файла");
//                                return null;
//                            }
//
//                            @Override
//                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                                String clientPath = pathHolder.getClientPath();
//                                System.out.println("Путь перед выходом:" + clientPath);
//                                pathHolder.setClientPath(Factory.giveStepBackPath(clientPath));
//                                pathHolder.setServerPath(Factory.giveStepBackPath(pathHolder.getServerPath()));
//                                System.out.println("Вышли из дериктории клиента: "+ pathHolder.getClientPath());
//                                System.out.println("Вышли из директории сервера: " + pathHolder.getServerPath());
//                                return FileVisitResult.CONTINUE;                            }
//                        }
//                        );
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//
//                }
//        System.out.println("В конце директория клиента: " + pathHolder.getClientPath());
//        System.out.println("В конце директория сервера: " + pathHolder.getServerPath());
//    };

//    private void writeIntoChannel(Path path) {
//        System.out.printf("Файл %s будет записан в канал и размещен в папке %s на сервере", path.getFileName().toString(), pathHolder.getServerPath());
//        System.out.println();
//        try {
//            CloudWrappedObject cwo = CloudWrappedObject.wrapFile(path, pathHolder.getClientPath(), pathHolder.getServerPath());
//            cf.channel().writeAndFlush(cwo).addListener((ChannelFutureListener) channelFuture -> {
//                System.out.println("Writing Complete!");
//                requestFilesList(pathHolder.getServerPath());
//            });
//        } catch (IOException a) {
//            System.out.println("Ошибка записи");
//            a.printStackTrace();
//        }
//    }



}
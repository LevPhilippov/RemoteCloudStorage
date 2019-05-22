package com.filippov;

import com.filippov.Handlers.ClientHandlersInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Network{

    public static final String HOST = "localhost";
    public static final int PEER_PORT = 8189;
    private static Network ourInstance = new Network();
    public static Network getInstance() {return ourInstance;}
    private static PathHolder pathHolder = new PathHolder();
    private ChannelFuture cf;
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
    public static MessageService messageService;
    private Thread networkThread;
    public static final Logger LOGGER = LogManager.getLogger(ourInstance.getClass().getCanonicalName());
    private String login;

    public void startNetwork(AuthData authData, LogController logController) {

        bossGroup = new NioEventLoopGroup(1);
        bootstrap = new Bootstrap();
        networkThread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    bootstrap.group(bossGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ClientHandlersInitializer())
                            .option(ChannelOption.SO_KEEPALIVE, true);

                    cf = bootstrap.connect("127.0.0.1", 8189);
                    cf.addListener((ChannelFutureListener) channelFuture -> {
                        if(!channelFuture.isSuccess()) {
                            LOGGER.warn("{}: Connnection failed!", login);
                        } else {
                            messageService.setSingleServiseMessage("Успешное подключение!");
                            LOGGER.info("{}: успешное подключение!", login);
                            //метод на изменение статуса сети
                            requestAuth(authData, logController);
                        }

                    }).sync();

                    cf.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    LOGGER.info("{}: Thread interruption exeption!", login);
                    LOGGER.error(e.getMessage());
                }
                finally {
                    shutdown();
                }
            }
        });
        networkThread.start();
    }

    public boolean networkIsActive() {
        return networkThread != null && networkThread.isAlive();
    }

    public void requestAuth(AuthData authData, LogController logController) {
        if(networkThread == null) {
            LOGGER.info("{}: Запускаю сеть!", login);
            messageService.setSingleServiseMessage("Запускаю сеть!");
            startNetwork(authData, logController);
        } else if (networkIsActive()) {
            LOGGER.info("{}: Отправлены авторизационные данные", login);
            messageService.setSingleServiseMessage("Отправлены авторизационные данные!");
            cf.channel().writeAndFlush(authData);
        }
    }

    public void shutdown() {
        cf.channel().closeFuture();
        bossGroup.shutdownGracefully();
        networkThread =null;
    }

    /**
     * Принимает на вход список Path и команду {@link Request} на их обработку.
     * @param os  List String-ключей для ClientMap
     * @param requestType  команда для обработки списка файлов
     * */

    public void filesHandler(ObservableList<String> os, Request.RequestType requestType) {
            os.stream().map(pathHolder.getClientPathMap()::get).forEach((path -> {
                if (Files.exists(path)) {
                    try {
                        Files.walkFileTree(path, new MyFileVisitor(cf.channel(), requestType)
                        );
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }));
        requestFilesListFromServer(getPathHolder().getServerPath());
    }

    /**
    * Метод отправляет запрос {@link Request} к серверу на получение списка файлов в указанной дидектории на сервере
     *
     * */

    public void requestFilesListFromServer(Path path) {
        Request request = new Request().setRequestType(Request.RequestType.FILELIST).setServerPath(Paths.get("root").toFile());
        if (path!=null) {
            request.setServerPath(path.toFile());
        }
        LOGGER.info("{}: Запрос списка файлов на сервере по адресу: {}", login, request.getServerPath().toString());
        messageService.setSingleServiseMessage("Запрос списка файлов на сервере по адресу: " + request.getServerPath().toString());
        cf.channel().writeAndFlush(request);
    }

    public void sendFilesRequest(ObservableList<String> os, Request.RequestType requestType) {
        List <File> filesList = new ArrayList<>();
        os.stream().map(key -> pathHolder.getServerPathMap().get(key).toFile()).forEach(filesList::add);
        cf.channel().writeAndFlush(new Request()
                .setRequestType(requestType)
                .setFileList(filesList));
    }

    public void sendPropertyRequest(String key) {
        File file = pathHolder.getServerPathMap().get(key).toFile();
        cf.channel().writeAndFlush(new Request()
                .setRequestType(Request.RequestType.PROPERTY).setServerPath(file));
    }

    public PathHolder getPathHolder() {
        return pathHolder;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import javafx.collections.ObservableList;

import javax.net.ssl.SSLException;
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
    private Network(){}
    private static PathHolder pathHolder = new PathHolder();
    private ChannelFuture cf;
    private EventLoopGroup bossGroup;
    private Bootstrap bootstrap;
    private static Controller controller;
    private static Thread networkThread;


    public void startNetwork(String login, String password) {
        bossGroup = new NioEventLoopGroup(2);
        bootstrap = new Bootstrap();
        networkThread = new Thread(new Runnable() {
            @Override
            public void run(){
                try {
                    // Configure SSL.
                    final SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                    //SSL

                    bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    sslCtx.newHandler(socketChannel.alloc(), HOST, PEER_PORT),
                                    new LoggingHandler("EndLogger", LogLevel.INFO),
                                    new ObjectDecoder(ClientWrappedFileHandler.byteBufferSize+1024*1024,ClassResolvers.cacheDisabled(null)),
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
                            System.out.println("Connection success! \n + Тайм-аут соединения" + sslCtx.sessionTimeout() + " секунд." );
                            //метод на изменение статуса сети
                            requestAuth(login, password);
                        }

                    }).sync();
                    cf.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    System.out.println("Thread interruption exeption!");
                    e.printStackTrace();
                } catch (SSLException o) {
                    System.out.println("SSL Exception!");
                    o.printStackTrace();
                }
                finally {
                    shutdown();
                }
            }
        });
        networkThread.setDaemon(true);
        networkThread.start();
    }

    public void requestAuth(String login, String password) {
        if(networkThread == null) {
            System.out.println("Запускаю сеть!");
            startNetwork(login, password);
        } else if (networkThread.isAlive()) {
            System.out.println("Отправлены авторизационные данные!");
            AuthData authData = new AuthData(login, password);
            cf.channel().writeAndFlush(authData);
        }
    }

    public void shutdown() {
        cf.channel().closeFuture();
        bossGroup.shutdownGracefully();
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
                        e.printStackTrace();
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
        System.out.println("Запрос списка файлов на сервере по адресу: " + request.getServerPath().toString());
        cf.channel().writeAndFlush(request);
    }

    public void sendFilesRequest(ObservableList<String> os, Request.RequestType requestType) {
        List <File> filesList = new ArrayList<>();
        os.stream().map(key -> pathHolder.getServerPathMap().get(key).toFile()).forEach(filesList::add);

        System.out.println("Запрос файлов из облака " + filesList);
        cf.channel().writeAndFlush(new Request()
                .setRequestType(requestType)
                .setFileList(filesList));
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